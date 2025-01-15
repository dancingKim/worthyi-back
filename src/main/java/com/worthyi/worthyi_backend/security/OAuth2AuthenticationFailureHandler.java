package com.worthyi.worthyi_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error("OAuth2 Authentication Exception: {}", exception.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.error(ApiStatus.UNAUTHORIZED, "Authentication failed: " + exception.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}