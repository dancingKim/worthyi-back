package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.common.ApiStatus;

public class ApiResponse<T> {
    private int code;       // Body에 들어갈 세부 코드
    private String message; // Body에 들어갈 메시지
    private T data;         // 실제 데이터

    private ApiStatus apiStatus; // ★ 추가: 어떤 ApiStatus인지 저장

    public ApiResponse(ApiStatus status, T data) {
        this.code = status.getCode();     // ex) 40121
        this.message = status.getMessage();
        this.data = data;
        this.apiStatus = status;          // 추가
    }

    // 메시지를 커스터마이징할 수 있는 생성자 추가
    public ApiResponse(ApiStatus status, String message, T data) {
        this.code = status.getCode();
        this.message = (message != null ? message : status.getMessage());
        this.data = data;
        this.apiStatus = status; // 추가
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

    public ApiStatus getApiStatus() {
        return apiStatus;
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