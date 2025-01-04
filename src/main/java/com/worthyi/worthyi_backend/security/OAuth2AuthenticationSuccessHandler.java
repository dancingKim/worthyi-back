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
        
        log.debug("Creating JWT token for authenticated user: {}", authentication.getName());
        String jwtToken = jwtTokenProvider.createToken(authentication);
        
        String email = jwtTokenProvider.getEmailFromToken(jwtToken);
        log.debug("Email extracted from token: {}", email);

        log.debug("Creating refresh token");
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        log.info("Saving refresh token to Redis");
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken,
                jwtTokenProvider.getRefreshTokenValidTime(), TimeUnit.MILLISECONDS);

        Optional<Cookie> oCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                .findFirst();
        log.debug("Redirect URI cookie found: {}", oCookie.isPresent());

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenValidTime() / 1000));
        response.addCookie(refreshTokenCookie);
        log.debug("Refresh token cookie added to response");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        log.debug("Refresh token cookie creation time (UTC): {}", now);

        response.addHeader("Refresh-Token", refreshToken);
        
        String redirectUri = oCookie.map(Cookie::getValue).orElseGet(() -> LOCAL_REDIRECT_URL);
        log.info("Redirecting to: {}", redirectUri);
        
        response.sendRedirect(redirectUri + "/sociallogin?token=" + jwtToken);
        log.info("=== OAuth2 Authentication Success Handler End ===");
    }
}