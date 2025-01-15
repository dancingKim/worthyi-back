package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        log.info("로그아웃 요청 시작");
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization 헤더: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("잘못된 Authorization 헤더");
        return ApiResponse.error(ApiStatus.BAD_REQUEST, "Authorization header is missing or invalid");
        }

        String accessToken = authHeader.replace("Bearer ", "");
        log.debug("추출된 액세스 토큰: {}", accessToken);
        
        authService.validateAccessToken(accessToken);

        log.info("로그아웃 처리 시작");
        authService.logout(accessToken);
        log.info("로그아웃 성공");
        return ApiResponse.success(ApiStatus.LOGOUT_SUCCESS.getMessage());
    }

    @PostMapping("/token/refresh")
    public ApiResponse<?> refreshTokens(HttpServletRequest request, HttpServletResponse response) {

        log.info("토큰 갱신 요청 시작");

        // HTTP-ONLY 쿠키에서 refreshToken 읽기

        Optional<Cookie> refreshTokenCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst());

        if (refreshTokenCookie.isEmpty()) {
            log.error("Refresh 토큰 누락");
            return ApiResponse.error(ApiStatus.MISSING_REFRESH_TOKEN, "Refresh token is missing in cookies");
        }

        String refreshToken = refreshTokenCookie.get().getValue();
        log.debug("Refresh 토큰: {}", refreshToken);

        authService.validateRefreshToken(refreshToken);

        try {
            log.info("토큰 갱신 처리 시작");
            String message = authService.refreshTokens(refreshToken, response);
            log.info("토큰 갱신 성공");
            return ApiResponse.success(message);
        } catch (IllegalArgumentException e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ApiResponse.error(ApiStatus.TOKEN_MISMATCH, e.getMessage());
        }
    }
}
