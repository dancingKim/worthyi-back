package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public void logout(String accessToken) {
        String email = jwtTokenProvider.getEmailFromToken(accessToken);

        // Redis에서 Refresh Token 삭제
        redisTemplate.delete("refresh:" + email);
        log.info("Refresh token deleted for email: {}", email);

        // Access Token을 블랙리스트 처리하여 만료될 때까지 유효하지 않도록 설정
        long expiration = jwtTokenProvider.getExpiration(accessToken) - System.currentTimeMillis();
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        log.debug("Logout time (UTC): {}", now);
        log.debug("Access token expiration time (UTC): {}", expiration);
    }

    public String refreshTokens(String refreshToken, HttpServletResponse response) {
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String redisRefreshToken = redisTemplate.opsForValue().get("refresh:" + email);

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        if (redisRefreshToken != null && redisRefreshToken.equals(refreshToken)) {
            List<GrantedAuthority> authorities = userService.getUserAuthoritiesByEmail(email);
            String newAccessToken = jwtTokenProvider.createToken(authentication, authorities);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

            // 새로운 Refresh Token을 Redis에 저장
            redisTemplate.opsForValue().set(
                    "refresh:" + email,
                    newRefreshToken,
                    jwtTokenProvider.getRefreshTokenValidTime(),
                    TimeUnit.MILLISECONDS
            );

            response.setHeader("Authorization", "Bearer " + newAccessToken);
            response.setHeader("Refresh-Token", newRefreshToken);

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            log.debug("Tokens refreshed at (UTC): {}", now);
            log.debug("New access token expiration time (UTC): {}", jwtTokenProvider.getExpiration(newAccessToken));
            log.debug("New refresh token expiration time (UTC): {}", jwtTokenProvider.getExpiration(newRefreshToken));

            return "Tokens refreshed successfully";
        }

        throw new IllegalArgumentException("The refresh token does not match the stored token");
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken);
    }

    public boolean isAccessTokenValid(String accessToken) {
        return jwtTokenProvider.validateToken(accessToken);
    }
}
