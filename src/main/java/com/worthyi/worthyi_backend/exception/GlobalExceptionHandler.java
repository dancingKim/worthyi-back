package com.worthyi.worthyi_backend.exception;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return ApiResponse.error(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ApiResponse<?> handleSecurityException(Exception e) {
        log.error("Security Exception: {}", e.getMessage());
        ApiStatus status = (e instanceof AccessDeniedException) ? 
            ApiStatus.FORBIDDEN : ApiStatus.UNAUTHORIZED;
        return ApiResponse.error(status, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ApiResponse.error(ApiStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");
    }
} 