package com.worthyi.worthyi_backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2AuthenticationSuccessHandler Starts");

        // JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(authentication);
        log.info("jwtToken = {}", jwtToken);

        String email = authentication.getName();
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken,
                jwtTokenProvider.getRefreshTokenValidTime(), TimeUnit.MILLISECONDS);

        // 토큰을 응답 헤더에 추가
        response.addHeader("Authorization", "Bearer " + jwtToken);
        response.addHeader("Refresh-Token", refreshToken);

        // 인증 성공 후 리디렉션할 URL 설정
        response.sendRedirect("/dashboard");
    }
}
