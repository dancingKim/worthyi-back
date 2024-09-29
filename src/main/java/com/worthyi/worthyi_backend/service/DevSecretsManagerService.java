//package com.worthyi.worthyi_backend.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
//import software.amazon.awssdk.services.secretsmanager.model.GetRandomPasswordResponse;
//import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
//import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
//
//@Service
//@Profile("dev")
//public class DevSecretsManagerService {
//    private final SecretsManagerClient secretsManagerClient;
//
//    public DevSecretsManagerService(@Value("${cloud.aws.secretsmanager.profile}") String profileName,
//                                    @Value("${cloud.aws.secretsmanager.region}") String region) {
//        this.secretsManagerClient = SecretsManagerClient.builder()
//                .credentialsProvider(ProfileCredentialsProvider.create(profileName))
//                .region(Region.of(region))
//                .build();
//    }
//
//    public String getSecret(String secretName) {
//        GetSecretValueRequest request = GetSecretValueRequest.builder()
//                .secretId(secretName)
//                .build();
//        GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
//        return response.secretString();
//    }
//}
