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
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import static com.worthyi.worthyi_backend.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private static final String LOCAL_REDIRECT_URL = "worthyi:/";
    private static final long AUTH_CODE_TTL = 5*60;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Map<String, Object> attributes = principalDetails.getAttributes();

        // JWT 토큰 생성
        log.debug("Creating JWT token for authenticated user: {}", authentication.getName());

        String attributesJson =  objectMapper.writeValueAsString(attributes);


        String authCode = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("authCode:" + authCode, attributesJson, AUTH_CODE_TTL, TimeUnit.SECONDS);



        // Redirect URI 쿠키 검색
        Optional<Cookie> oCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                        .findFirst());
        log.debug("Redirect URI cookie found: {}", oCookie.isPresent());

        String redirectUri = oCookie.map(Cookie::getValue).orElseGet(() -> LOCAL_REDIRECT_URL);
        log.info("Redirecting to: {}", redirectUri);

        // 클라이언트로 리디렉션
        response.sendRedirect(redirectUri + "/sociallogin?code=" + authCode);
        log.info("=== OAuth2 Authentication Success Handler End ===");
    }
}