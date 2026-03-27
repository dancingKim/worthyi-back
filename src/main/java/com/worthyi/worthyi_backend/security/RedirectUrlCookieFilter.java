package com.worthyi.worthyi_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectUrlCookieFilter extends OncePerRequestFilter {
    public static final String REDIRECT_URI_PARAM = "redirect_url";
    private static final int MAX_AGE = 3600;
    private final OAuth2RedirectValidator redirectValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/oauth2/authorization")) {
            String requestedRedirectUrl = request.getParameter(REDIRECT_URI_PARAM);
            String safeRedirectUrl = redirectValidator.resolveRedirectUriOrDefault(requestedRedirectUrl);
            if (requestedRedirectUrl != null && !redirectValidator.isAllowed(requestedRedirectUrl)) {
                log.warn("Blocked untrusted redirect_url: {}", requestedRedirectUrl);
            }

            String encodedRedirectUrl = URLEncoder.encode(safeRedirectUrl, StandardCharsets.UTF_8);
            response.addHeader(
                    "Set-Cookie",
                    String.format(
                            "%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=Lax",
                            REDIRECT_URI_PARAM,
                            encodedRedirectUrl,
                            MAX_AGE
                    )
            );
        }
        filterChain.doFilter(request, response);
    }
}
