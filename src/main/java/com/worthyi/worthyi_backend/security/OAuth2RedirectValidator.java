package com.worthyi.worthyi_backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OAuth2RedirectValidator {
    private final Set<String> allowedRedirectOrigins;
    private final Set<String> allowedCustomSchemes;
    private final String defaultRedirectUri;

    public OAuth2RedirectValidator(
            @Value("${app.security.oauth2.allowed-redirect-origins:http://localhost:3000,https://www.worthyilife.com,https://worthyilife.com}") List<String> allowedRedirectOrigins,
            @Value("${app.security.oauth2.allowed-redirect-schemes:worthyi}") List<String> allowedCustomSchemes,
            @Value("${app.security.oauth2.default-redirect-uri:worthyi:/}") String defaultRedirectUri
    ) {
        this.allowedRedirectOrigins = allowedRedirectOrigins.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        this.allowedCustomSchemes = allowedCustomSchemes.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.defaultRedirectUri = defaultRedirectUri;
    }

    public String resolveRedirectUriOrDefault(String candidate) {
        if (isAllowed(candidate)) {
            return candidate.trim();
        }
        return defaultRedirectUri;
    }

    public boolean isAllowed(String candidate) {
        if (!StringUtils.hasText(candidate)) {
            return false;
        }

        String trimmed = candidate.trim();
        if (trimmed.length() > 512) {
            return false;
        }

        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (Exception e) {
            return false;
        }

        String scheme = uri.getScheme();
        if (!StringUtils.hasText(scheme)) {
            return false;
        }

        String normalizedScheme = scheme.toLowerCase();
        if (allowedCustomSchemes.contains(normalizedScheme)) {
            return true;
        }

        if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
            return false;
        }

        if (!StringUtils.hasText(uri.getHost())) {
            return false;
        }

        String origin = normalizedScheme + "://" + uri.getHost().toLowerCase();
        if (uri.getPort() != -1) {
            origin += ":" + uri.getPort();
        }

        return allowedRedirectOrigins.contains(origin);
    }
}
