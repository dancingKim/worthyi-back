package com.worthyi.worthyi_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        // 요청에서 JWT 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);
        log.debug("Resolved token: {}", token);

        // 토큰이 유효한지 확인
        if (token != null && jwtTokenProvider.validateToken(token)) {
            log.info("Valid token: {}", token);
            // 토큰으로부터 인증 정보 가져오기
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            log.debug("Extracted authentication: {}", authentication);
            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("JWT tocken is invalid or missing");
        }
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}