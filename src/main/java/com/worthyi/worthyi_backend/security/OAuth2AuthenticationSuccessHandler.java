package com.worthyi.worthyi_backend.security;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private final OAuth2RedirectValidator redirectValidator;
    private static final String LOCAL_REDIRECT_URL = "worthyi:/";
    private static final long AUTH_CODE_TTL = 5*60;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuth2AuthenticationSuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            StringRedisTemplate redisTemplate,
            OAuth2RedirectValidator redirectValidator
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.redirectValidator = redirectValidator;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Map<String, Object> attributes = principalDetails.getAttributes();

        attributes.remove("nonce");
        attributes.remove("at_hash");
        attributes.remove("aud");
        attributes.remove("nonce_supported");
        attributes.remove("email_verified");
        attributes.remove("auth_time");
        attributes.remove("iss");
        attributes.remove("iat");
        attributes.remove("exp");
        attributes.remove("nonce");

        String attributesJson =  objectMapper.writeValueAsString(attributes);
        String authCode = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("authCode:" + authCode, attributesJson, AUTH_CODE_TTL, TimeUnit.SECONDS);

        Optional<Cookie> oCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                        .findFirst());

        String decodedRedirectUri = oCookie
                .map(Cookie::getValue)
                .map(this::decodeCookieValue)
                .orElse(LOCAL_REDIRECT_URL);
        String redirectUri = redirectValidator.resolveRedirectUriOrDefault(decodedRedirectUri);
        if (!redirectValidator.isAllowed(decodedRedirectUri)) {
            log.warn("Blocked untrusted redirect URI from cookie: {}", decodedRedirectUri);
        }

        String normalizedRedirectUri = redirectUri.endsWith("/")
                ? redirectUri.substring(0, redirectUri.length() - 1)
                : redirectUri;
        response.sendRedirect(normalizedRedirectUri + "/sociallogin?code=" + authCode);
    }

    private String decodeCookieValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode redirect cookie value.");
            return LOCAL_REDIRECT_URL;
        }
    }
}
