package com.worthyi.worthyi_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("dev")
public class AwsSecretsIntegrationTest {

    @Value("${DB_URL}")
    private String dbUrl;

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Test
    public void testSecretsAreLoaded() {
        assertNotNull(dbUrl,"DB_URL should not be null");
        assertNotNull(dbUsername,"DB_USERNAME should not be null");
        assertNotNull(dbPassword,"DB_PASSWORD should not be null");
    }
}
