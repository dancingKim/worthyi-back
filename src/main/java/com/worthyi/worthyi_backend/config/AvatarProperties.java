package com.worthyi.worthyi_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.avatar")
public class AvatarProperties {
    private String googleAiApiKey;
    private String googleAiDefaultModelPreset;
    private String googleAiBestModel;
    private String googleAiBalancedModel;
    private String googleAiFastModel;
    private String googleAiEndpoint;
    private String s3Bucket;
    private String s3Prefix;
    private String defaultReferencePath;
    private long signedUrlMinutes;
    private int maxPromptLength;
}
