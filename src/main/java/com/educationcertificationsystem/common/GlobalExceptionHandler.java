package com.educationcertificationsystem.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Result<Void> handleBusinessException(RuntimeException ex) {
        return Result.error(ex.getMessage());
    }
}
