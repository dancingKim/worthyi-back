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
    private String name;
    private String email;
    private String profile;
    private String providerUserId;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) { // OAuth2 공급자별로 사용자 정보 생성
            case "google" -> ofGoogle(attributes, registrationId);
            default -> throw new IllegalStateException("Unexpected value: " + registrationId);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes, String registrationId) {
        String providerUserId = registrationId + (String) attributes.getOrDefault("sub", "");
        String name = (String) attributes.getOrDefault("name", "");
        String email = (String) attributes.getOrDefault("email", "");
        String profile = (String) attributes.getOrDefault("picture", "");
        return OAuth2UserInfo.builder()
                .name(name)
                .email(email)
                .profile(profile)
                .providerUserId(providerUserId)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .username(name)
                .providerUserId(providerUserId)
                .authorities("ROLE_USER")
                .build();
    }
}
