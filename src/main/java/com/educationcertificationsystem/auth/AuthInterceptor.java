package com.educationcertificationsystem.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.model.entity.SysLoginSession;
import com.educationcertificationsystem.model.entity.SysRole;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.SysUserRole;
import com.educationcertificationsystem.role.service.SysRoleService;
import com.educationcertificationsystem.role.service.SysUserRoleService;
import com.educationcertificationsystem.user.service.SysLoginSessionService;
import com.educationcertificationsystem.user.service.SysUserService;
import com.educationcertificationsystem.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SysLoginSessionService loginSessionService;

    private final SysUserService userService;

    private final SysUserRoleService userRoleService;

    private final SysRoleService roleService;

    private final ObjectMapper objectMapper;

    public AuthInterceptor(
        SysLoginSessionService loginSessionService,
        SysUserService userService,
        SysUserRoleService userRoleService,
        SysRoleService roleService,
        ObjectMapper objectMapper
    ) {
        this.loginSessionService = loginSessionService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            writeFailure(response, 401, "未登录或登录已过期");
            return false;
        }

        String token = authorization.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            writeFailure(response, 401, "未登录或登录已过期");
            return false;
        }

        SysLoginSession session = loginSessionService.getOne(new LambdaQueryWrapper<SysLoginSession>()
            .eq(SysLoginSession::getAccessTokenHash, TokenUtils.hashToken(token))
            .eq(SysLoginSession::getRevokedFlag, 0)
            .eq(SysLoginSession::getIsDeleted, 0)
            .gt(SysLoginSession::getExpireTime, LocalDateTime.now())
            .last("limit 1"));
        if (session == null) {
            writeFailure(response, 401, "登录状态已失效，请重新登录");
            return false;
        }

        SysUser user = userService.getById(session.getUserId());
        if (user == null || user.getIsDeleted() == 1 || user.getUserStatus() != 1) {
            writeFailure(response, 401, "当前账号不可用");
            return false;
        }

        Set<String> roleCodes = loadRoleCodes(user.getId());
        CurrentUserContext.set(new CurrentUserInfo(user.getId(), user.getUsername(), user.getRealName(), roleCodes));
        session.setLastActiveTime(LocalDateTime.now());
        loginSessionService.updateById(session);

        if (handler instanceof HandlerMethod handlerMethod) {
            RequireRoles requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);
            if (requireRoles == null) {
                requireRoles = handlerMethod.getBeanType().getAnnotation(RequireRoles.class);
            }
            if (requireRoles != null && !CurrentUserContext.require().hasAnyRole(requireRoles.value())) {
                writeFailure(response, 403, "没有权限访问该资源");
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private Set<String> loadRoleCodes(Long userId) {
        List<SysUserRole> relations = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, userId)
            .eq(SysUserRole::getIsDeleted, 0));
        if (relations.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).toList();
        return roleService.listByIds(roleIds).stream()
            .filter(role -> role.getIsDeleted() == 0 && role.getStatus() == 1)
            .map(SysRole::getRoleCode)
            .collect(Collectors.toSet());
    }

    private void writeFailure(HttpServletResponse response, Integer code, String message) throws Exception {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.failure(code, message)));
    }
}
