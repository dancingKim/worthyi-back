package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.config.AvatarProperties;
import com.worthyi.worthyi_backend.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarImageStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AvatarProperties avatarProperties;

    public String uploadGeneratedImage(UUID userId, byte[] imageBytes, String mimeType) {
        String bucket = requireBucket();
        String key = buildObjectKey(userId, mimeType);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(mimeType)
                .cacheControl("private, max-age=31536000")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(imageBytes));
        return key;
    }

    public byte[] download(String key) {
        ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(requireBucket())
                        .key(key)
                        .build()
        );
        return responseBytes.asByteArray();
    }

    public String createReadUrl(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(requireBucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(Math.max(1, avatarProperties.getSignedUrlMinutes())))
                .getObjectRequest(request)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void delete(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(requireBucket())
                .key(key)
                .build());
    }

    public void deleteQuietly(String key) {
        try {
            delete(key);
        } catch (Exception exception) {
            log.warn("Failed to delete avatar image from S3. key={}", key, exception);
        }
    }

    private String buildObjectKey(UUID userId, String mimeType) {
        String prefix = avatarProperties.getS3Prefix();
        String normalizedPrefix = StringUtils.hasText(prefix) ? prefix.replaceAll("/+$", "") : "avatars/users";
        return normalizedPrefix + "/" + userId + "/" + UUID.randomUUID() + fileExtensionFor(mimeType);
    }

    private String requireBucket() {
        if (!StringUtils.hasText(avatarProperties.getS3Bucket())) {
            throw new CustomException(ApiStatus.SERVICE_UNAVAILABLE, "Avatar image bucket is not configured");
        }
        return avatarProperties.getS3Bucket();
    }

    private String fileExtensionFor(String mimeType) {
        if ("image/png".equalsIgnoreCase(mimeType)) {
            return ".png";
        }
        if ("image/jpeg".equalsIgnoreCase(mimeType) || "image/jpg".equalsIgnoreCase(mimeType)) {
            return ".jpg";
        }
        if ("image/webp".equalsIgnoreCase(mimeType)) {
            return ".webp";
        }
        return ".bin";
    }
}
