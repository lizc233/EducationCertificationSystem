package com.educationcertificationsystem.config;

import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.common.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception, HttpServletResponse response) {
        response.setStatus(resolveStatus(exception.getCode()));
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException exception, HttpServletResponse response) {
        response.setStatus(400);
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("参数校验失败");
        return ApiResponse.failure(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleMessageNotReadableException(HttpMessageNotReadableException exception, HttpServletResponse response) {
        response.setStatus(400);
        return ApiResponse.failure(400, "请求体不是合法的 JSON");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception, HttpServletResponse response) {
        log.error("Unhandled exception", exception);
        response.setStatus(500);
        return ApiResponse.failure(500, "系统异常，请稍后重试");
    }

    private int resolveStatus(Integer code) {
        if (code == null) {
            return 400;
        }
        if (code == 401 || code == 403 || code == 404) {
            return code;
        }
        return 400;
    }
}
