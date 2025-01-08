package com.worthyi.worthyi_backend.security;

import com.worthyi.worthyi_backend.model.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Builder
@Getter
@ToString
public class OAuth2UserInfo {
    private String provider;
    private String sub;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) { // OAuth2 공급자별로 사용자 정보 생성
            case "google" -> ofGoogle(registrationId, attributes);
            case "apple" -> ofApple(registrationId, attributes);
            default -> throw new IllegalStateException("Unexpected value: " + registrationId);
        };
    }

    private static OAuth2UserInfo ofGoogle(String registrationId, Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .provider(registrationId)
                .sub((String) attributes.get("sub"))
                .build();
    }

    private static OAuth2UserInfo ofApple(String registrationId, Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .provider(registrationId)
                .sub((String) attributes.get("sub"))
                .build();
    }

    public User toEntity() {
        return User.builder()
                .provider(provider)
                .sub(sub)
                .authorities("ROLE_USER")
                .build();
    }
}
