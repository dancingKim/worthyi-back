package com.worthyi.worthyi_backend.security;

import lombok.Getter;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import java.util.Map;

/**
 * OIDC 전용 (Apple 등)
 */
@Getter
public class OidcPrincipalDetails extends PrincipalDetails implements OidcUser {

    private final OidcUser oidcDelegate;  

    public OidcPrincipalDetails(OidcUser oidcDelegate, 
                                Map<String, Object> mergedAttributes, 
                                String attributeKey) {
        // 부모(PrincipalDetails)의 생성자 호출
        super(mergedAttributes, attributeKey);
        this.oidcDelegate = oidcDelegate;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcDelegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcDelegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcDelegate.getIdToken();
    }

    @Override
    public String getName() {
        // userId를 반환하도록 부모 로직 사용
        return super.getName();
    }
}