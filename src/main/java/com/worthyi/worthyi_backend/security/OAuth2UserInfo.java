package com.worthyi.worthyi_backend.security;

import com.worthyi.worthyi_backend.model.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * OAuth2 / OIDC 로그인 시, provider + sub 등 최소 정보만 보관
 */
@Builder
@Getter
@ToString
public class OAuth2UserInfo {
    private String provider;  // "google" / "apple" 등
    private String sub;       // OAuth2 provider에서 유저 식별값

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> ofGoogle(registrationId, attributes);
            case "apple" -> ofApple(registrationId, attributes);
            default -> throw new IllegalStateException("Unexpected provider: " + registrationId);
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
        // username 컬럼 없어졌으므로, sub + provider 만으로 유저 식별
        return User.builder()
                .provider(provider)
                .sub(sub)
                .authorities("ROLE_USER")
                .build();
    }
}