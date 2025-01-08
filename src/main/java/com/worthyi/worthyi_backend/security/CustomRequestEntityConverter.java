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

@Getter
@Component
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {
    //기본 OAuth2AuthorizationCodeGrantRequestEntityConverter를 사용하여 기본 요청 엔티티를 생성
    private final OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

    // Apple OAuth2 설정 값들을 주입받음
    @Value("${APPLE_KID}")
    private String keyId; // Apple 키 ID

    @Value("${APPLE_TID}")
    private String teamId; // Apple 팀 ID

    @Value("${APPLE_CLIENT_ID}")
    private String clientId; // Apple 클라이언트 ID

    @Value("${APPLE_CLIENT_SECRET}")
    private String privateKeyContent; // Apple 개인 키 내용

    @Value("${apple.url}")
    private String url; // Apple URL

    public CustomRequestEntityConverter() {
        this.defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
    }

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest req) {
        // 기본 요청 엔티티를 생성
        RequestEntity<?> entity = defaultConverter.convert(req);
        String registrationId = req.getClientRegistration().getRegistrationId();
        Object body = entity.getBody();

        // Apple OAuth2 요청인 경우 클라이언트 비밀을 설정
        if (registrationId.contains("apple") && body instanceof MultiValueMap) {
            @SuppressWarnings("unchecked")
            MultiValueMap<String, String> params = (MultiValueMap<String, String>) body;
            try {
                // 클라이언트 비밀 생성
                params.set("client_secret", createClientSecret());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new RequestEntity<>(params, entity.getHeaders(), entity.getMethod(), entity.getUrl());
        }
        return entity;
    }

    // 개인 키를 가져오는 메서드
    public PrivateKey getPrivateKey() throws IOException {
        try (PEMParser pemParser = new PEMParser(new StringReader(privateKeyContent))) {
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(object);
        }
    }

    // 클라이언트 비밀(JWT)을 생성하는 메서드
    public String createClientSecret() throws IOException {
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", keyId); // JWT 헤더에 키 ID 설정
        jwtHeader.put("alg", "ES256"); // JWT 헤더에 알고리즘 설정

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(teamId) // 발행자 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5))) // 만료 시간 설정
                .setAudience(url) // 대상 설정
                .setSubject(clientId) // 주제 설정
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256) // 개인 키로 서명
                .compact();
    }
}