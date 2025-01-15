package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import com.worthyi.worthyi_backend.exception.CustomException;
import com.worthyi.worthyi_backend.common.ApiStatus;
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
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            // Redis에서 Refresh Token 삭제
            redisTemplate.delete("refresh:" + userId);
            log.info("Refresh token deleted for userId: {}", userId);

            // Access Token을 블랙리스트 처리하여 만료될 때까지 유효하지 않도록 설정
            long expiration = jwtTokenProvider.getExpiration(accessToken) - System.currentTimeMillis();
            redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            log.debug("Logout time (UTC): {}", now);
            log.debug("Access token expiration time (UTC): {}", expiration);
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new CustomException(ApiStatus.INTERNAL_SERVER_ERROR, "Logout process failed");
        }
    }

    public String refreshTokens(String refreshToken, HttpServletResponse response) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            String redisRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);

            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

            if (redisRefreshToken != null && redisRefreshToken.equals(refreshToken)) {
                List<GrantedAuthority> authorities = userService.getUserAuthoritiesByUserId(userId);
                String newAccessToken = jwtTokenProvider.createToken(authentication, authorities);
                String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

                // 새로운 Refresh Token을 Redis에 저장
                redisTemplate.opsForValue().set(
                        "refresh:" + userId,
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

            throw new CustomException(ApiStatus.TOKEN_MISMATCH, "The refresh token does not match the stored token");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new CustomException(ApiStatus.INTERNAL_SERVER_ERROR, "Token refresh process failed");
        }
    }

    public void validateRefreshToken(String refreshToken) {
        jwtTokenProvider.validateRefreshToken(refreshToken);
    }

    public void validateAccessToken(String accessToken) {
        jwtTokenProvider.validateAccessToken(accessToken);
    }
}
