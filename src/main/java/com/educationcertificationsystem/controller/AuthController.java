package com.educationcertificationsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.auth.CurrentUserContext;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.common.BusinessException;
import com.educationcertificationsystem.dto.auth.LoginRequest;
import com.educationcertificationsystem.service.MenuAccessService;
import com.educationcertificationsystem.model.entity.SysLoginSession;
import com.educationcertificationsystem.model.entity.SysRole;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.SysUserRole;
import com.educationcertificationsystem.role.service.SysRoleService;
import com.educationcertificationsystem.role.service.SysUserRoleService;
import com.educationcertificationsystem.user.service.SysLoginSessionService;
import com.educationcertificationsystem.user.service.SysUserService;
import com.educationcertificationsystem.util.PasswordUtils;
import com.educationcertificationsystem.util.TokenUtils;
import com.educationcertificationsystem.vo.auth.AuthLoginResponse;
import com.educationcertificationsystem.vo.auth.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService userService;

    private final SysRoleService roleService;

    private final SysUserRoleService userRoleService;

    private final SysLoginSessionService loginSessionService;

    private final MenuAccessService menuAccessService;

    public AuthController(
        SysUserService userService,
        SysRoleService roleService,
        SysUserRoleService userRoleService,
        SysLoginSessionService loginSessionService,
        MenuAccessService menuAccessService
    ) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.loginSessionService = loginSessionService;
        this.menuAccessService = menuAccessService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        SysUser user = userService.getOne(new LambdaQueryWrapper<SysUser>()
            .and(wrapper -> wrapper.eq(SysUser::getUsername, request.getAccount())
                .or()
                .eq(SysUser::getPhone, request.getAccount())
                .or()
                .eq(SysUser::getEmail, request.getAccount()))
            .eq(SysUser::getIsDeleted, 0)
            .last("limit 1"));

        if (user == null || !PasswordUtils.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getUserStatus() != 1) {
            throw new BusinessException("当前账号已停用");
        }

        Set<String> roleCodes = loadRoleCodes(user.getId());
        if (roleCodes.isEmpty()) {
            throw new BusinessException("当前账号未分配角色");
        }

        revokeActiveSessions(user.getId());

        String token = TokenUtils.generateToken();
        LocalDateTime now = LocalDateTime.now();
        SysLoginSession session = new SysLoginSession();
        session.setUserId(user.getId());
        session.setAccessTokenHash(TokenUtils.hashToken(token));
        session.setLoginTime(now);
        session.setExpireTime(now.plusDays(7));
        session.setLastActiveTime(now);
        session.setRevokedFlag(0);
        session.setLoginIp(httpServletRequest.getRemoteAddr());
        session.setClientType("WEB");
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setIsDeleted(0);
        loginSessionService.save(session);

        user.setLastLoginAt(now);
        user.setLastLoginIp(httpServletRequest.getRemoteAddr());
        user.setUpdatedAt(now);
        userService.updateById(user);

        AuthLoginResponse response = new AuthLoginResponse();
        response.setToken(token);
        response.setUserInfo(buildUserInfo(user, roleCodes));
        response.setPermissions(buildPermissions(roleCodes));
        response.setMenuPaths(menuAccessService.getAccessiblePaths(roleCodes));
        response.setMenus(menuAccessService.getAccessibleMenuTree(roleCodes));
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            SysLoginSession session = loginSessionService.getOne(new LambdaQueryWrapper<SysLoginSession>()
                .eq(SysLoginSession::getAccessTokenHash, TokenUtils.hashToken(token))
                .eq(SysLoginSession::getRevokedFlag, 0)
                .last("limit 1"));
            if (session != null) {
                session.setRevokedFlag(1);
                session.setLogoutTime(LocalDateTime.now());
                session.setUpdatedAt(LocalDateTime.now());
                loginSessionService.updateById(session);
            }
        }
        CurrentUserContext.clear();
        return ApiResponse.success();
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("ok");
    }

    @GetMapping("/menus")
    public ApiResponse<List<com.educationcertificationsystem.vo.auth.MenuNodeVO>> menus() {
        return ApiResponse.success(menuAccessService.getAccessibleMenuTree(CurrentUserContext.require().getRoleCodes()));
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring(7).trim();
    }

    private void revokeActiveSessions(Long userId) {
        List<SysLoginSession> sessions = loginSessionService.list(new LambdaQueryWrapper<SysLoginSession>()
            .eq(SysLoginSession::getUserId, userId)
            .eq(SysLoginSession::getRevokedFlag, 0)
            .eq(SysLoginSession::getIsDeleted, 0));
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach(session -> {
            session.setRevokedFlag(1);
            session.setLogoutTime(now);
            session.setUpdatedAt(now);
        });
        if (!sessions.isEmpty()) {
            loginSessionService.updateBatchById(sessions);
        }
    }

    private Set<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getIsDeleted, 0))
            .stream()
            .map(SysUserRole::getRoleId)
            .toList();
        return roleService.listByIds(roleIds).stream()
            .filter(role -> role.getIsDeleted() == 0 && role.getStatus() == 1)
            .map(SysRole::getRoleCode)
            .collect(Collectors.toSet());
    }

    private UserInfoVO buildUserInfo(SysUser user, Set<String> roleCodes) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setAccountId(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setRole(roleCodes.stream().findFirst().orElse(""));
        userInfoVO.setDepartment(user.getRemark());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setStatus(user.getUserStatus());
        return userInfoVO;
    }

    private List<String> buildPermissions(Set<String> roleCodes) {
        if (roleCodes.isEmpty()) {
            return List.of();
        }
        return menuAccessService.getPermissionCodes(roleCodes);
    }
}
