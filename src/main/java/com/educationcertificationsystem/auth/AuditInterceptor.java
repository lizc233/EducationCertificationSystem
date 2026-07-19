package com.educationcertificationsystem.auth;

import com.educationcertificationsystem.model.entity.SysOperationLog;
import com.educationcertificationsystem.system.service.SysOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "auditStartTime";

    private final SysOperationLogService operationLogService;

    public AuditInterceptor(SysOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startTime = request.getAttribute(START_TIME);
        long duration = startTime instanceof Long value ? System.currentTimeMillis() - value : 0L;
        CurrentUserInfo currentUser = CurrentUserContext.get();

        SysOperationLog log = new SysOperationLog();
        if (currentUser != null) {
            log.setOperatorUserId(currentUser.getUserId());
            log.setOperatorName(currentUser.getRealName());
        }
        log.setLogType(resolveLogType(request.getMethod(), request.getRequestURI()));
        log.setModuleName(resolveModuleName(request.getRequestURI()));
        log.setBizType(request.getMethod());
        log.setRequestUri(request.getRequestURI());
        log.setRequestMethod(request.getMethod());
        log.setRequestParams(request.getQueryString());
        log.setSuccessFlag(ex == null && response.getStatus() < 400 ? 1 : 0);
        log.setErrorMessage(ex == null ? null : ex.getMessage());
        log.setDurationMs((int) duration);
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        log.setIsDeleted(0);
        operationLogService.save(log);
    }

    private String resolveLogType(String method, String uri) {
        if (uri.startsWith("/api/auth/login")) {
            return "LOGIN";
        }
        if (uri.startsWith("/api/auth/logout")) {
            return "LOGOUT";
        }
        return switch (method) {
            case "GET" -> "QUERY";
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "OTHER";
        };
    }

    private String resolveModuleName(String uri) {
        if (uri.startsWith("/api/auth")) {
            return "Authentication";
        }
        if (uri.startsWith("/api/user")) {
            return "User Management";
        }
        if (uri.startsWith("/api/org")) {
            return "Organization";
        }
        if (uri.startsWith("/api/system/params")) {
            return "System Params";
        }
        if (uri.startsWith("/api/system/dicts")) {
            return "Dictionaries";
        }
        if (uri.startsWith("/api/system/logs")) {
            return "Operation Logs";
        }
        return "API";
    }
}
