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
        return (String) attributes.get(attributeKey);
    }

    @Override
    public String getName() {
        // attributeKey로부터 가져오되, 없으면 userId로 fallback
        String attr = (String) attributes.get(attributeKey);
        return attr == null ? (String) attributes.get("userId") : attr;
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
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; 
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; 
    }

    @Override
    public boolean isEnabled() {
        return true; 
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A getAttribute(String name) {
        return (A) attributes.get(name);
    }
}