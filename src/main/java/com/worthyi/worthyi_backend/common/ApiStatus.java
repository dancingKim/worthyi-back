package com.worthyi.worthyi_backend.common;

public enum ApiStatus {
    // Success Codes
    SUCCESS(200, "Success"),
    CREATED(201, "Resource created successfully"),
    LOGOUT_SUCCESS(200, "Successfully logged out"),
    TOKEN_REFRESHED(200, "Tokens refreshed successfully"),

    // Client Error Codes
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized access"),
    INVALID_TOKEN(401, "Invalid token"),
    EXPIRED_TOKEN(401, "Expired token"),
    TOKEN_MISMATCH(401, "Token does not match stored refresh token"),
    FORBIDDEN(403, "Access forbidden"),
    NOT_FOUND(404, "Resource not found"),
    USER_NOT_FOUND(404, "User not found"),
    VALIDATION_ERROR(422, "Validation error"),
    MISSING_REFRESH_TOKEN(400, "Refresh token is missing"),

    // Conflict or Resource issues
    CONFLICT(409, "Conflict occurred"),
    EMAIL_ALREADY_EXISTS(409, "Email already exists"),
    USERNAME_ALREADY_EXISTS(409, "Username already exists"),

    // Server Error Codes
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service unavailable");

    private final int code;
    private final String message;

    ApiStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
