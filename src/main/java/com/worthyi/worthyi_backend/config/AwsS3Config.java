package com.worthyi.worthyi_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Bean
    public S3Client s3Client(@Value("${spring.cloud.aws.region.static:ap-northeast-2}") String awsRegion) {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${spring.cloud.aws.region.static:ap-northeast-2}") String awsRegion) {
        return S3Presigner.builder()
                .region(Region.of(awsRegion))
                .build();
    }
}
