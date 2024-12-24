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
import java.net.CookieManager;

@Slf4j
@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {
    public static final String REDIRECT_URI_PARAM = "redirect_url";
    private static final int MAX_AGE = 3600;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURL = request.getRequestURL().toString();

        if (requestURL.startsWith("/oauth2/authorization")) {
            String redirectUrl = request.getParameter(REDIRECT_URI_PARAM);
            Cookie cookie = new Cookie(REDIRECT_URI_PARAM, redirectUrl);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(MAX_AGE);
            response.addCookie(cookie);
            try {
            } catch (Exception ex) {
                logger.error("Could not set user authentification in security context", ex);
                log.info("Unauthorized request");
            }
        }
        filterChain.doFilter(request, response);
    }
}
