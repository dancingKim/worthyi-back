package com.worthyi.worthyi_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {
    public static final String REDIRECT_URI_PARAM = "redirect_url";
    private static final int MAX_AGE = 3600;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("=== RedirectUrl Cookie Filter Start ===");
        String requestURL = request.getRequestURL().toString();
        log.debug("Request URL: {}", requestURL);

        if (requestURL.startsWith("/oauth2/authorization")) {
            String redirectUrl = request.getParameter(REDIRECT_URI_PARAM);
            log.debug("Redirect URL parameter found: {}", redirectUrl);
            
            Cookie cookie = new Cookie(REDIRECT_URI_PARAM, redirectUrl);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(MAX_AGE);
            response.addCookie(cookie);
            log.debug("Redirect URL cookie set: {}, maxAge: {}", redirectUrl, MAX_AGE);
            
            try {
                log.debug("Processing OAuth2 authorization request");
            } catch (Exception ex) {
                log.error("Failed to set user authentication in security context", ex);
                log.warn("Unauthorized request detected");
            }
        }
        
        log.info("=== RedirectUrl Cookie Filter End ===");
        filterChain.doFilter(request, response);
    }
}
