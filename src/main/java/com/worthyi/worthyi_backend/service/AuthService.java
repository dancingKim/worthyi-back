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
import com.worthyi.worthyi_backend.model.dto.TokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

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
    public TokenDto.Response getToken(String authCode) {
        try {
            String attributesJson = redisTemplate.opsForValue().get("authCode:" + authCode);
            if (attributesJson == null) {
                throw new CustomException(ApiStatus.NOT_FOUND, "Auth code not found");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> attributes = objectMapper.readValue(attributesJson, new TypeReference<Map<String, Object>>() {});
            String accessToken = jwtTokenProvider.createToken(attributes);
            String refreshToken = jwtTokenProvider.createRefreshToken(attributes);

            String userId = (String) attributes.get("userId");

            redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, jwtTokenProvider.getRefreshTokenValidTime(), TimeUnit.MILLISECONDS);
            redisTemplate.delete("authCode:" + authCode);

            return TokenDto.Response.of(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("Token generation failed: {}", e.getMessage());
            throw new CustomException(ApiStatus.INTERNAL_SERVER_ERROR, "Token generation process failed");
        }
    }

    public TokenDto.RefreshResponse refreshTokens(String refreshToken, HttpServletResponse response) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            String redisRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);

            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

            if (redisRefreshToken != null && redisRefreshToken.equals(refreshToken)) {
                List<GrantedAuthority> authorities = userService.getUserAuthoritiesByUserId(userId);
                String newAccessToken = jwtTokenProvider.createToken(authentication, authorities);

                // 새로운 Refresh Token을 Redis에 저장

                TokenDto.RefreshResponse tokens = TokenDto.RefreshResponse.of(newAccessToken);

                return tokens;
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
