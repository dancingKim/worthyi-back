package com.worthyi.worthyi_backend.common;

public enum ApiStatus {
    // Success Codes
    SUCCESS(200, "Success", 200),
    CREATED(201, "Resource created successfully", 201),
    LOGOUT_SUCCESS(200, "Successfully logged out", 200),
    TOKEN_REFRESHED(200, "Tokens refreshed successfully", 200),

    // Client Error Codes
    BAD_REQUEST(400, "Bad request", 400),
    UNAUTHORIZED(401, "Unauthorized access", 401),
    INVALID_TOKEN(401, "Invalid token", 401),
    EXPIRED_TOKEN(401, "Expired token", 401),

    // ↓ Body(=code)는 40121, 실제 HTTP코드(=httpStatus)는 401
    EXPIRED_ACCESS_TOKEN(40121, "Expired access token", 401),
    EXPIRED_REFRESH_TOKEN(40122, "Expired refresh token", 401),

    TOKEN_MISMATCH(401, "Token does not match stored refresh token", 401),
    FORBIDDEN(403, "Access forbidden", 403),
    NOT_FOUND(404, "Resource not found", 404),
    USER_NOT_FOUND(404, "User not found", 404),
    VALIDATION_ERROR(422, "Validation error", 422),
    MISSING_REFRESH_TOKEN(400, "Refresh token is missing", 400),

    // Conflict or Resource issues
    CONFLICT(409, "Conflict occurred", 409),
    EMAIL_ALREADY_EXISTS(409, "Email already exists", 409),
    USERNAME_ALREADY_EXISTS(409, "Username already exists", 409),

    // Server Error Codes
    INTERNAL_SERVER_ERROR(500, "Internal server error", 500),
    SERVICE_UNAVAILABLE(503, "Service unavailable", 503),

    // 추가할 상태 코드들
    AVATAR_NOT_FOUND(404, "Avatar not found", 404),
    VILLAGE_NOT_FOUND(404, "Village not found", 404),
    PLACE_NOT_FOUND(404, "Place not found", 404),
    ACTION_TEMPLATE_NOT_FOUND(404, "Action template not found", 404),
    ACTION_SAVE_FAILED(500, "Failed to save action", 500),
    CHILD_ACTION_NOT_FOUND(404, "Child action not found", 404),
    ADULT_ACTION_NOT_FOUND(404, "Adult action not found", 404),
    NOT_AUTHORIZED_TO_DELETE(403, "Not authorized to delete this action", 403),
    ACTION_DELETE_FAILED(500, "Failed to delete action", 500),

    // Body = 4003, HTTP = 400
    INVALID_ACTION_RELATIONSHIP(4003, "Adult action does not belong to the specified child action", 400);

    /**
     * body에 들어갈 상세 에러 코드
     */
    private final int code;
    private final String message;

    /**
     * 실제 HTTP Status 코드 (3자리)
     */
    private final int httpStatus;

    ApiStatus(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * JSON 바디에 들어갈 에러/응답 코드
     */
    public int getCode() {
        return code;
    }

    /**
     * JSON 바디에 들어갈 기본 메시지
     */
    public String getMessage() {
        return message;
    }

    /**
     * 실제 HTTP Status (3자리 표준 코드)
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}