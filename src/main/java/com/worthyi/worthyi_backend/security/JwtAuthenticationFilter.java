package com.worthyi.worthyi_backend.security;

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

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        String token = jwtTokenProvider.resolveToken(request);
        log.debug("Resolved token: {}", token);

        if (token != null) {

            String isLougout = redisTemplate.opsForValue().get("blacklist:"+token);

            if (isLougout != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            } else if(jwtTokenProvider.validateToken(token)) {
                log.info("Valid token: {}", token);

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                log.debug("Extracted authentication: {}", authentication);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (jwtTokenProvider.isTokenExpired(token)){
                log.warn("JWT token is expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }
        } else{
            log.warn("JWT token is missing");
        }

        filterChain.doFilter(request, response);
    }
}
