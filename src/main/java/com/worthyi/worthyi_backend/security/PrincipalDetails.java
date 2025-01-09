package com.worthyi.worthyi_backend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class PrincipalDetails implements OAuth2User, UserDetails {

    private final Map<String, Object> attributes;
    private final String attributeKey;

    public PrincipalDetails(Map<String, Object> attributes, String attributeKey) {
        this.attributes = attributes;
        this.attributeKey = attributeKey;
    }

    @Override
    public String getUsername() {
        return (String) attributes.get("userId");
    }

    @Override
    public String getName() {
        String attr = (String) attributes.get(attributeKey);
        return attr.toString(); // Fallback to username if attribute is null
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getPassword() {
        return ""; // OAuth2 로그인 사용자의 경우 비밀번호 없음
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // userRoles가 null일 경우 빈 리스트 반환
        String roles = (String) attributes.get("authorities");
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return java.util.Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료되지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠기지 않음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료되지 않음
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화됨
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A getAttribute(String name) {
        return (A) attributes.get(name);
    }
}