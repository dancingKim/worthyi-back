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
import com.worthyi.worthyi_backend.exception.CustomException;
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
            String token = jwtTokenProvider.resolveToken(request);
            
            if (token != null) {
                String isLogout = redisTemplate.opsForValue().get("blacklist:" + token);
                log.debug("Token blacklist check: {}", isLogout != null ? "blacklisted" : "valid");

                if (isLogout != null) {
                    log.warn("Attempt to use blacklisted token");
                    sendErrorResponse(response, ApiStatus.UNAUTHORIZED, "로그아웃된 토큰입니다");
                    return;
                }

                try {
                    // 액세스 토큰 검증만 수행
                    jwtTokenProvider.validateAccessToken(token);
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (CustomException e) {
                    sendErrorResponse(response, e.getStatus(), e.getMessage());
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendErrorResponse(response, ApiStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다");
        }
    }
}
