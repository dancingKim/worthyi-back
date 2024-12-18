package com.worthyi.worthyi_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    private void sendErrorResponse(HttpServletResponse response, ApiStatus status, String message) throws IOException {
        response.setStatus(status.getCode());
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<?> apiResponse = ApiResponse.error(status, message);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
            String token = jwtTokenProvider.resolveToken(request);
            log.debug("Resolved token: {}", token);

            if (token != null) {

                String isLougout = redisTemplate.opsForValue().get("blacklist:" + token);

                if (isLougout != null) {
                    sendErrorResponse(response, ApiStatus.UNAUTHORIZED, "로그아웃된 토큰입니다");
                    return;
                } else if (!jwtTokenProvider.validateToken(token)) {
                    sendErrorResponse(response, ApiStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다");
                    return;
                } else if (jwtTokenProvider.isTokenExpired(token)) {
                    log.warn("JWT token is expired");
                    sendErrorResponse(response, ApiStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다");
                    return;
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                log.debug("Extracted authentication: {}", authentication);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("JWT token is missing");
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생", e);
            sendErrorResponse(response, ApiStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다");
            return;
        }
    }
}
