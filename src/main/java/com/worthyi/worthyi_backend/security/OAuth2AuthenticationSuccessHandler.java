package com.worthyi.worthyi_backend.security;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.Duration;

import static com.worthyi.worthyi_backend.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private static final String LOCAL_REDIRECT_URL = "worthyi:/";

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("=== OAuth2 Authentication Success Handler Start ===");
        log.info("requestURI: {}", request.getRequestURI());
        log.info("requestURL: {}", request.getRequestURL());
        log.info("request protocol: {}", request.getProtocol());
        log.info("request remoteAddr: {}", request.getRemoteAddr());
        log.info("request remoteHost: {}", request.getRemoteHost());
        log.info("request remotePort: {}", request.getRemotePort());
        log.info("request remoteUser: {}", request.getRemoteUser());
        log.info("request remotePort: {}", request.getRemotePort());

        // JWT 토큰 생성
        log.debug("Creating JWT token for authenticated user: {}", authentication.getName());
        String jwtToken = jwtTokenProvider.createToken(authentication);

        // 사용자 ID 추출
        String userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        log.debug("Email extracted from token: {}", userId);

        // Refresh Token 생성
        log.debug("Creating refresh token");
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Refresh Token을 Redis에 저장
        log.info("Saving refresh token to Redis");
        long refreshTokenValidTime = jwtTokenProvider.getRefreshTokenValidTime();
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, refreshTokenValidTime, TimeUnit.MILLISECONDS);

        log.debug("Refresh token valid time (ms): {}", refreshTokenValidTime);
        log.debug("Refresh token will expire at (UTC): {}", ZonedDateTime.now(ZoneId.of("UTC")).plus(Duration.ofMillis(refreshTokenValidTime)));

        // Redirect URI 쿠키 검색
        Optional<Cookie> oCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                        .findFirst());
        log.debug("Redirect URI cookie found: {}", oCookie.isPresent());

        // 수동으로 Set-Cookie 헤더 설정 (SameSite=None; Secure)
        String setCookieHeader = String.format("refreshToken=%s; Path=/; Domain=api-dev.worthyilife.com; HttpOnly; Secure; SameSite=None; Max-Age=%d",
                refreshToken, refreshTokenValidTime / 1000);
        response.addHeader("Set-Cookie", setCookieHeader);
        log.info("Set-Cookie header: {}", setCookieHeader);
        log.debug("Refresh token cookie added to response with SameSite=None; Secure");

        // 추가 헤더 설정 (필요 시)
        response.addHeader("Refresh-Token", refreshToken);
        log.info("Refresh-Token header: {}", refreshToken);
        // Redirect URI 결정
        String redirectUri = oCookie.map(Cookie::getValue).orElseGet(() -> LOCAL_REDIRECT_URL);
        log.info("Redirecting to: {}", redirectUri);

        // 클라이언트로 리디렉션
        response.sendRedirect(redirectUri + "/sociallogin?token=" + jwtToken);
        log.info("=== OAuth2 Authentication Success Handler End ===");
    }
}