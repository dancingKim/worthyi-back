package com.worthyi.worthyi_backend.exception;

import com.worthyi.worthyi_backend.common.ApiStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ApiStatus status;

    public CustomException(ApiStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    public CustomException(ApiStatus status, String message) {
        super(message);
        this.status = status;
    }
} 