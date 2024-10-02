package com.worthyi.worthyi_backend.security;

import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrincipalDetails implements OAuth2User, UserDetails {

    private User user;
    private Map<String, Object> attributes;
    private String attributeKey;

    public PrincipalDetails(User user, Map<String, Object> attributes, String attributeKey) {
        this.user = user;
        this.attributes = attributes;
        this.attributeKey = attributeKey;
    }

    @Override
    public String getUsername() {
        return user.getUserUuid().toString();
    }

    @Override
    public String getName() {
        return attributes.get(attributeKey).toString();
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
        // 사용자의 권한 반환
        return Collections.singletonList(new SimpleGrantedAuthority(user.getAuthorityNm()));
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
    public <A> A getAttribute(String name) {
        return (A) attributes.get(name);
    }
}