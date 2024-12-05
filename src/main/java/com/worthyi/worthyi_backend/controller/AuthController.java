package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(ApiStatus.BAD_REQUEST, "Authorization header is missing or invalid");
        }

        String accessToken = authHeader.replace("Bearer ", "");
        if (!authService.isAccessTokenValid(accessToken)) {
            return ApiResponse.error(ApiStatus.INVALID_TOKEN, "Invalid access token");
        }

        authService.logout(accessToken);
        return ApiResponse.success(ApiStatus.LOGOUT_SUCCESS.getMessage());
    }

    @PostMapping("/token/refresh")
    public ApiResponse<?> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("refresh_token");

        if (refreshToken == null) {
            return ApiResponse.error(ApiStatus.MISSING_REFRESH_TOKEN, "Please provide a refresh token in the header");
        }

        if (!authService.isRefreshTokenValid(refreshToken)) {
            return ApiResponse.error(ApiStatus.INVALID_TOKEN, "The provided refresh token is not valid");
        }

        try {
            String message = authService.refreshTokens(refreshToken, response);
            return ApiResponse.success(message);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ApiStatus.TOKEN_MISMATCH, e.getMessage());
        }
    }
}
