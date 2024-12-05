package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

<<<<<<< Updated upstream
    public void logout(String accessToken) {
        String email = jwtTokenProvider.getEmailFromToken(accessToken);

        // Redis에서 Refresh Token 삭제
=======
    /**
     * 사용자를 로그아웃 처리하는 메서드
     *
     * @param accessToken 로그아웃할 Access Token
     * @param email       사용자 이메일
     */
    public void logout(String accessToken, String email){
        // Refresh Token 삭제
>>>>>>> Stashed changes
        redisTemplate.delete("refresh:" + email);
        log.info("Refresh token deleted for email: {}", email);

<<<<<<< Updated upstream
        // Access Token을 블랙리스트 처리하여 만료될 때까지 유효하지 않도록 설정
        long expiration = jwtTokenProvider.getExpiration(accessToken) - System.currentTimeMillis();
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    public String refreshTokens(String refreshToken, HttpServletResponse response) {
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String redisRefreshToken = redisTemplate.opsForValue().get("refresh:" + email);

        if (redisRefreshToken != null && redisRefreshToken.equals(refreshToken)) {
            List<GrantedAuthority> authorities = userService.getUserAuthoritiesByEmail(email);
            String newAccessToken = jwtTokenProvider.createToken(email, authorities);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

            // 새로운 Refresh Token을 Redis에 저장
            redisTemplate.opsForValue().set(
                    "refresh:" + email,
                    newRefreshToken,
                    jwtTokenProvider.getRefreshTokenValidTime(),
                    TimeUnit.MILLISECONDS
            );

            response.setHeader("Authorization", "Bearer " + newAccessToken);
            response.setHeader("Refresh-Token", newRefreshToken);

            return "Tokens refreshed successfully";
        }

        throw new IllegalArgumentException("The refresh token does not match the stored token");
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken);
    }

    public boolean isAccessTokenValid(String accessToken) {
        return jwtTokenProvider.validateToken(accessToken);
=======
        // Access Token 만료 시간 계산
        long expiration = jwtTokenProvider.getExpiration(accessToken) - System.currentTimeMillis();
        log.info("Access token expiration in milliseconds: {}", expiration);

        // Access Token이 아직 유효한 경우에만 블랙리스트에 추가
        if (expiration > 0) {
            redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
            log.info("Access token added to blacklist with expiration: {} ms", expiration);
        } else {
            log.warn("Access token already expired, cannot add to blacklist.");
        }
>>>>>>> Stashed changes
    }
}
