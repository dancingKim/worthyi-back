package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.common.ApiStatus;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public ApiResponse(ApiStatus status, T data) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = data;
    }

    // 메시지를 커스터마이징할 수 있는 생성자 추가
    public ApiResponse(ApiStatus status, String message, T data) {
        this.code = status.getCode();
        this.message = message != null ? message : status.getMessage();
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiStatus.SUCCESS, data);
    }

    // 에러 응답 - 기본 메시지 사용
    public static <T> ApiResponse<T> error(ApiStatus status) {
        return new ApiResponse<>(status, status.getMessage(), null);
    }

    // 에러 응답 - 커스텀 메시지 사용
    public static <T> ApiResponse<T> error(ApiStatus status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
