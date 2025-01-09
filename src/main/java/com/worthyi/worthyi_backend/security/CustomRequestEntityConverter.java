package com.worthyi.worthyi_backend.security;

import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Getter
@Component
@Slf4j
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {
    private final OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

    @Value("${APPLE_KID}")
    private String keyId;

    @Value("${APPLE_TID}")
    private String teamId;

    @Value("${APPLE_CLIENT_ID}")
    private String clientId;

    @Value("${APPLE_CLIENT_SECRET}")
    private String privateKeyContent;

    @Value("${apple.url}")
    private String url;

    public CustomRequestEntityConverter() {
        this.defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
        log.debug("CustomRequestEntityConverter initialized with: keyId={}, teamId={}, clientId={}, url={}", keyId,
                teamId, clientId, url);
    }

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest req) {
        log.debug("=== CustomRequestEntityConverter convert method called ===");
        log.debug("Converting OAuth2AuthorizationCodeGrantRequest for registrationId: {}",
                req.getClientRegistration().getRegistrationId());
        // Authorization Code와 State 정보 로그 추가
        String authorizationCode = req.getAuthorizationExchange().getAuthorizationResponse().getCode();
        String state = req.getAuthorizationExchange().getAuthorizationResponse().getState();

        log.debug("Authorization Code: {}", authorizationCode);
        log.debug("State: {}", state);
        RequestEntity<?> entity = defaultConverter.convert(req);
        String registrationId = req.getClientRegistration().getRegistrationId();

        // 제네릭 타입을 명시적으로 지정하여 캐스팅
        if (entity != null && entity.getBody() instanceof MultiValueMap) {
            @SuppressWarnings("unchecked")
            MultiValueMap<String, String> params = (MultiValueMap<String, String>) entity.getBody();

            if (registrationId.contains("apple")) {
                try {
                    log.debug("Creating client secret for Apple OAuth2");
                    params.set("client_secret", createClientSecret());
                    log.debug("Client secret set successfully");
                } catch (IOException e) {
                    log.error("Error creating client secret: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
                return new RequestEntity<>(params, entity.getHeaders(), entity.getMethod(), entity.getUrl());
            }
        }
        return entity;
    }

    public PrivateKey getPrivateKey() throws IOException {
        log.debug("=== CustomRequestEntityConverter getPrivateKey method called ===");
        log.debug("Getting private key for Apple OAuth2");
        try {
            // PEM 헤더와 푸터 제거
            String privateKeyPEM = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64 디코딩
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            // PrivateKey 객체 생성
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            log.debug("Private key obtained successfully");
            return privateKey;
        } catch (Exception e) {
            log.error("Error obtaining private key: {}", e.getMessage());
            throw new IOException("Failed to obtain private key", e);
        }
    }

    public String createClientSecret() throws IOException {
        log.debug("=== CustomRequestEntityConverter createClientSecret method called ===");
        log.debug("Creating JWT for client secret");
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", keyId);
        jwtHeader.put("alg", "ES256");
        log.debug("JWT Header: {}", jwtHeader);
        String jwt = Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(teamId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5)))
                .setAudience(url)
                .setSubject(clientId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();

        log.debug("Payload: iss={}, iat={}, exp={}, aud={}, sub={}", teamId, new Date(),
                new Date(System.currentTimeMillis() + (1000 * 60 * 5)), url, clientId);
        log.debug("JWT created successfully: {}", jwt);
        return jwt;
    }
}