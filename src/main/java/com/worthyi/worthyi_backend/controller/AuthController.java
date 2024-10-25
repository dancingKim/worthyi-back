package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import com.worthyi.worthyi_backend.service.AuthService;
import com.worthyi.worthyi_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
           String accessToken = authHeader.replace("Bearer ", "");
           String email = jwtTokenProvider.getEmailFromToken(accessToken);

           authService.logout(accessToken, email);
           return ResponseEntity.ok().build();
        } else{
            return ResponseEntity.badRequest().build();

        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshTokens(HttpServletRequest request, HttpServletResponse response)
    {
        String refreshToken = request.getHeader("refresh_token");

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);

            String redisRefreshToken = redisTemplate.opsForValue().get("refresh:" + email);
            List<GrantedAuthority> authorities = userService.getUserAuthoritiesByEmail(email);
            if (redisRefreshToken != null && redisRefreshToken.equals(refreshToken)) {

                // 새로운 Access Token과 Refresh Token을 발급

                String newAccessToken = jwtTokenProvider.createToken(email, authorities);
                String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

                // 새로운 Refresh Token을 Redis에 저장
                redisTemplate.opsForValue().set("refresh:" + email,
                        newRefreshToken, jwtTokenProvider.getRefreshTokenValidTime(), TimeUnit.MILLISECONDS);

                response.setHeader("Authorization", "Bearer " + newAccessToken);
                response.setHeader("Refresh-Token", newRefreshToken);

                return ResponseEntity.ok("Tokens refreshed successfully.");

            }

            return ResponseEntity.ok("Tokens refreshed successfully.");
        }
        return ResponseEntity.status(401).body("Invalid or expired refresh token");
    }
}
