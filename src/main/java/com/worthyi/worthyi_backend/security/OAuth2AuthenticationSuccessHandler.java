package com.worthyi.worthyi_backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2AuthenticationSuccessHandler Starts");
        log.info("onAuthenticationSuccess Starts");
        // JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(authentication);
        log.info("jwtToken = {}",jwtToken);

        // 토큰을 HTTP 응답 헤더에 추가하여 클라이언트에게 전달
        response.addHeader("Authorization", "Bearer " + jwtToken);

        // 인증 성공 후 리디렉션할 URL 설정
        response.sendRedirect("/dashboard");
    }
}