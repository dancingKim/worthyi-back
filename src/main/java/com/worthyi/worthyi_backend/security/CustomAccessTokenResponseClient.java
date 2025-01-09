package com.worthyi.worthyi_backend.security;

import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class CustomAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        // Apple의 토큰 엔드포인트에 요청을 보내고 응답을 받습니다.
        Map<String, Object> response = restTemplate.postForObject(
                authorizationGrantRequest.getClientRegistration().getProviderDetails().getTokenUri(),
                authorizationGrantRequest, Map.class);

        // 응답을 파싱하여 OAuth2AccessTokenResponse로 변환합니다.
        return parseAppleTokenResponse(response);
    }

    private OAuth2AccessTokenResponse parseAppleTokenResponse(Map<String, Object> response) {
        String accessToken = (String) response.get("access_token");
        String idToken = (String) response.get("id_token");
        Long expiresIn = ((Number) response.get("expires_in")).longValue();

        return OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(expiresIn)
                .additionalParameters(Map.of("id_token", idToken))
                .build();
    }
} 