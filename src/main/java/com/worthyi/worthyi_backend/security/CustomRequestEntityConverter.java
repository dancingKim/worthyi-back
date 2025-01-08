package com.worthyi.worthyi_backend.security;

import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

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
        log.debug("CustomRequestEntityConverter initialized with: keyId={}, teamId={}, clientId={}, url={}", keyId, teamId, clientId, url);
    }

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest req) {
        log.debug("Converting OAuth2AuthorizationCodeGrantRequest for registrationId: {}", req.getClientRegistration().getRegistrationId());
        RequestEntity<?> entity = defaultConverter.convert(req);
        String registrationId = req.getClientRegistration().getRegistrationId();
        Object body = entity.getBody();

        if (registrationId.contains("apple") && body instanceof MultiValueMap) {
            @SuppressWarnings("unchecked")
            MultiValueMap<String, String> params = (MultiValueMap<String, String>) body;
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
        return entity;
    }

    public PrivateKey getPrivateKey() throws IOException {
        log.debug("Getting private key for Apple OAuth2");
        try (PEMParser pemParser = new PEMParser(new StringReader(privateKeyContent))) {
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKey privateKey = converter.getPrivateKey(object);
            log.debug("Private key obtained successfully");
            return privateKey;
        }
    }

    public String createClientSecret() throws IOException {
        log.debug("Creating JWT for client secret");
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", keyId);
        jwtHeader.put("alg", "ES256");

        String jwt = Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(teamId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5)))
                .setAudience(url)
                .setSubject(clientId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
        log.debug("JWT created successfully: {}", jwt);
        return jwt;
    }
}