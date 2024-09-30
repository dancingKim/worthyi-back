package com.worthyi.worthyi_backend.security;

import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // JWT 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(authentication);

        // 토큰을 HTTP 응답 헤더 또는 쿠키에 추가하여 클라이언트에게 전달
        response.addHeader("Authorization", "Bearer " + jwtToken);

        // 로그인 후 리디렉션할 URL 설정
        response.sendRedirect("/dashboard");
    }
}
