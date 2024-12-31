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
        log.info("OAuth2 인증 성공 처리 시작");
        
        log.debug("JWT 토큰 생성 시작");
        String jwtToken = jwtTokenProvider.createToken(authentication);
        log.debug("생성된 JWT 토큰: {}", jwtToken);

        String email = jwtTokenProvider.getEmailFromToken(jwtToken);
        log.debug("토큰에서 추출한 이메일: {}", email);

        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        log.debug("Refresh 토큰 생성: {}", refreshToken);

        log.info("Redis에 Refresh 토큰 저장");
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken,
                jwtTokenProvider.getRefreshTokenValidTime(), TimeUnit.MILLISECONDS);

        Optional<Cookie> oCookie = Arrays.stream(request.getCookies()).filter(cookie ->
                cookie.getName().equals(REDIRECT_URI_PARAM)).findFirst();
        Optional<String> redirectUri = oCookie.map(Cookie::getValue);

        log.info("token {}", jwtToken);
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS 사용 시 설정
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenValidTime() / 1000));
        response.addCookie(refreshTokenCookie);

        
        


        // 토큰을 응답 헤더에 추가
        response.addHeader("Refresh-Token", refreshToken);

        log.info("redirectUri = {}", redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL));

        // 인증 성공 후 리디렉션할 URL 설정
        response.sendRedirect(redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL) + "/sociallogin?token=" + jwtToken);
        
        log.info("OAuth2 인증 성공 처리 완료");
    }
}