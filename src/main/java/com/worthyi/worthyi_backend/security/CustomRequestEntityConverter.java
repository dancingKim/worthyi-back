package com.worthyi.worthyi_backend.security;


import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.core.io.ClassPathResource;
import org.apache.commons.io.IOUtils;

@Getter
@Component
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {
    private final OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;
    private final String path;
    private final String keyId;
    private final String teamId;
    private final String clientId;
    private final String url;

    public CustomRequestEntityConverter(AppleProperties properties) {
        this.defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
        this.path = properties.getPath();
        this.keyId = properties.getKid();
        this.teamId = properties.getTid();
        this.clientId = properties.getCid();
        this.url = properties.getUrl();
    }
    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest req) {
        RequestEntity<?> entity = defaultConverter.convert(req);
        String registrationId = req.getClientRegistration().getRegistrationId();
        Object body = entity.getBody();

        if (registrationId.contains("apple") && body instanceof MultiValueMap) {
            @SuppressWarnings("unchecked")
            MultiValueMap<String, String> params = (MultiValueMap<String, String>) body;
            try {
                params.set("client_secret", createClientSecret());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new RequestEntity<>(params, entity.getHeaders(), entity.getMethod(), entity.getUrl());
        }
        return entity;
    }
    public PrivateKey getPrivateKey() throws IOException {

        ClassPathResource resource = new ClassPathResource(path);

        InputStream in = resource.getInputStream();
        PEMParser pemParser = new PEMParser(new StringReader(IOUtils.toString(in, StandardCharsets.UTF_8)));
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        return converter.getPrivateKey(object);
    }

    public String createClientSecret() throws IOException {
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", keyId);
        jwtHeader.put("alg", "ES256");

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(teamId)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간 - UNIX 시간
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5)))// 만료 시간
                .setAudience(url)
                .setSubject(clientId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }
}