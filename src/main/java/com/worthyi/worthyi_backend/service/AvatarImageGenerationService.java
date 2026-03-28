package com.worthyi.worthyi_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.config.AvatarProperties;
import com.worthyi.worthyi_backend.exception.CustomException;
import com.worthyi.worthyi_backend.model.entity.AvatarImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarImageGenerationService {

    private final AvatarProperties avatarProperties;
    private final AvatarImageStorageService avatarImageStorageService;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GeneratedAvatarResult generateAvatarImage(String prompt, String modelPreset, AvatarImage referenceAvatarImage) {
        validateConfiguration();

        String resolvedPrompt = normalizePrompt(prompt);
        String modelCode = resolveModelCode(modelPreset);
        ReferenceImage referenceImage = referenceAvatarImage == null
                ? loadDefaultReferenceImage()
                : loadStoredReferenceImage(referenceAvatarImage);

        String requestBody = buildRequestBody(resolvedPrompt, referenceImage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(avatarProperties.getGoogleAiEndpoint() + "/" + modelCode + ":generateContent"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("x-goog-api-key", avatarProperties.getGoogleAiApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CustomException(
                        ApiStatus.AVATAR_IMAGE_GENERATION_FAILED,
                        extractErrorMessage(response.body(), response.statusCode())
                );
            }

            return parseGeneratedImage(response.body(), modelCode);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CustomException(ApiStatus.AVATAR_IMAGE_GENERATION_FAILED, "Failed to generate avatar image");
        } catch (IOException exception) {
            throw new CustomException(ApiStatus.AVATAR_IMAGE_GENERATION_FAILED, "Failed to generate avatar image");
        }
    }

    private String buildRequestBody(String prompt, ReferenceImage referenceImage) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsNode = rootNode.putArray("contents");
        ObjectNode contentNode = contentsNode.addObject();
        ArrayNode partsNode = contentNode.putArray("parts");

        partsNode.addObject().put("text", buildSystemPrompt(prompt));
        ObjectNode inlineDataNode = partsNode.addObject().putObject("inline_data");
        inlineDataNode.put("mime_type", referenceImage.mimeType());
        inlineDataNode.put("data", Base64.getEncoder().encodeToString(referenceImage.bytes()));

        ObjectNode generationConfigNode = rootNode.putObject("generationConfig");
        ArrayNode responseModalities = generationConfigNode.putArray("responseModalities");
        responseModalities.add("TEXT");
        responseModalities.add("IMAGE");

        try {
            return objectMapper.writeValueAsString(rootNode);
        } catch (IOException exception) {
            throw new CustomException(ApiStatus.AVATAR_IMAGE_GENERATION_FAILED, "Failed to build avatar generation request");
        }
    }

    private GeneratedAvatarResult parseGeneratedImage(String responseBody, String modelCode) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode candidatesNode = rootNode.path("candidates");
            for (JsonNode candidateNode : candidatesNode) {
                JsonNode partsNode = candidateNode.path("content").path("parts");
                for (JsonNode partNode : partsNode) {
                    JsonNode inlineDataNode = partNode.path("inlineData");
                    if (inlineDataNode.isMissingNode() || inlineDataNode.isNull()) {
                        inlineDataNode = partNode.path("inline_data");
                    }

                    String imageData = inlineDataNode.path("data").asText(null);
                    if (!StringUtils.hasText(imageData)) {
                        continue;
                    }

                    String mimeType = inlineDataNode.path("mimeType").asText(null);
                    if (!StringUtils.hasText(mimeType)) {
                        mimeType = inlineDataNode.path("mime_type").asText("image/png");
                    }

                    return new GeneratedAvatarResult(Base64.getDecoder().decode(imageData), mimeType, modelCode);
                }
            }
        } catch (IOException exception) {
            throw new CustomException(ApiStatus.AVATAR_IMAGE_GENERATION_FAILED, "Failed to parse avatar image generation response");
        }

        throw new CustomException(ApiStatus.AVATAR_IMAGE_GENERATION_FAILED, "Google AI did not return an avatar image");
    }

    private ReferenceImage loadDefaultReferenceImage() {
        Resource resource = resourceLoader.getResource(avatarProperties.getDefaultReferencePath());
        try {
            return new ReferenceImage(resource.getInputStream().readAllBytes(), "image/png");
        } catch (IOException exception) {
            throw new CustomException(ApiStatus.AVATAR_IMAGE_GENERATION_FAILED, "Failed to load the default avatar reference image");
        }
    }

    private ReferenceImage loadStoredReferenceImage(AvatarImage referenceAvatarImage) {
        return new ReferenceImage(
                avatarImageStorageService.download(referenceAvatarImage.getImageKey()),
                referenceAvatarImage.getMimeType()
        );
    }

    private String buildSystemPrompt(String prompt) {
        return """
                Use the attached reference image as the same core character for the WorthyI mobile app.
                Keep the character identity, full-body framing, warm soft illustration style, and vertical composition close to 781 by 1024.
                Apply only the requested change unless a tiny adjustment is necessary to keep the result coherent.
                Return exactly one character.
                No text, no speech bubble, no frame, no extra objects, no background scene.
                The background must be a flat pure white background with no gradient, texture, pattern, props, or shadow.
                User request: %s
                """.formatted(prompt);
    }

    private String normalizePrompt(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new CustomException(ApiStatus.BAD_REQUEST, "Avatar generation prompt is required");
        }

        String normalizedPrompt = prompt.trim().replaceAll("\\s+", " ");
        int maxPromptLength = Math.max(1, avatarProperties.getMaxPromptLength());
        if (normalizedPrompt.length() > maxPromptLength) {
            return normalizedPrompt.substring(0, maxPromptLength);
        }
        return normalizedPrompt;
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(avatarProperties.getGoogleAiApiKey())) {
            throw new CustomException(ApiStatus.SERVICE_UNAVAILABLE, "Google AI API key is not configured");
        }
        if (!StringUtils.hasText(avatarProperties.getGoogleAiEndpoint())) {
            throw new CustomException(ApiStatus.SERVICE_UNAVAILABLE, "Google AI endpoint is not configured");
        }
    }

    private String resolveModelCode(String modelPreset) {
        String defaultPreset = StringUtils.hasText(avatarProperties.getGoogleAiDefaultModelPreset())
                ? avatarProperties.getGoogleAiDefaultModelPreset()
                : "BEST";

        String normalizedPreset = StringUtils.hasText(modelPreset)
                ? modelPreset.trim().toUpperCase()
                : defaultPreset.trim().toUpperCase();

        return switch (normalizedPreset) {
            case "BEST" -> avatarProperties.getGoogleAiBestModel();
            case "FAST" -> avatarProperties.getGoogleAiFastModel();
            case "BALANCED" -> avatarProperties.getGoogleAiBalancedModel();
            default -> throw new CustomException(ApiStatus.BAD_REQUEST, "Unsupported avatar model preset: " + modelPreset);
        };
    }

    private String extractErrorMessage(String responseBody, int statusCode) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String message = rootNode.path("error").path("message").asText(null);
            if (StringUtils.hasText(message)) {
                return message;
            }
        } catch (IOException exception) {
            log.warn("Failed to parse Google AI error response. statusCode={}", statusCode, exception);
        }
        return "Avatar image generation failed with status " + statusCode;
    }

    public record GeneratedAvatarResult(byte[] imageBytes, String mimeType, String modelCode) {
    }

    private record ReferenceImage(byte[] bytes, String mimeType) {
    }
}
