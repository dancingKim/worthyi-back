package com.worthyi.worthyi_backend.security;

import lombok.Getter;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import java.util.Map;

/**
 * This class wraps your existing PrincipalDetails logic
 * and also satisfies OidcUser for Apple or any other OIDC provider.
 */
@Getter
public class OidcPrincipalDetails extends PrincipalDetails implements OidcUser {

    private final OidcUser oidcDelegate;  

    public OidcPrincipalDetails(OidcUser oidcDelegate, 
                                Map<String, Object> mergedAttributes, 
                                String attributeKey) {
        // Pass the merged attributes to the parent PrincipalDetails constructor
        super(mergedAttributes, attributeKey);
        this.oidcDelegate = oidcDelegate;
    }

    /** OIDC-specific methods delegated to the original OidcUser. */

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

    /**
     * You can choose whether to override getName() with your own logic,
     * or use the delegate's name, or the parent logic.
     */
    @Override
    public String getName() {
        // For example, just use parent's logic that returns "userId"
        return super.getName();

        // Alternatively, you could use:
        // return oidcDelegate.getName();
    }
}