package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.exception.CustomException;
import com.worthyi.worthyi_backend.model.dto.AvatarImageDto;
import com.worthyi.worthyi_backend.model.entity.AvatarImage;
import com.worthyi.worthyi_backend.model.entity.User;
import com.worthyi.worthyi_backend.repository.AvatarImageRepository;
import com.worthyi.worthyi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarImageService {

    private static final String GENERATED_SOURCE_TYPE = "GENERATED";
    private static final String DEFAULT_REFERENCE_TYPE = "DEFAULT";
    private static final String CUSTOM_REFERENCE_TYPE = "CUSTOM";

    private final UserRepository userRepository;
    private final AvatarImageRepository avatarImageRepository;
    private final AvatarImageGenerationService avatarImageGenerationService;
    private final AvatarImageStorageService avatarImageStorageService;

    @Transactional(readOnly = true)
    public AvatarImageDto.CollectionResponse getAvatarImages(String userId) {
        UUID parsedUserId = UUID.fromString(userId);
        User user = getUserWithActiveAvatarImage(parsedUserId);
        List<AvatarImage> avatarImages = avatarImageRepository.findAllByUser_UserIdOrderByCreatedAtDesc(parsedUserId);
        return buildCollectionResponse(user, avatarImages);
    }

    @Transactional
    public AvatarImageDto.CollectionResponse createAvatarImage(String userId, AvatarImageDto.GenerateRequest request) {
        UUID parsedUserId = UUID.fromString(userId);
        User user = getUserWithActiveAvatarImage(parsedUserId);
        AvatarImage referenceAvatarImage = request.getReferenceAvatarImageId() == null
                ? null
                : getOwnedAvatarImage(parsedUserId, request.getReferenceAvatarImageId());

        AvatarImageGenerationService.GeneratedAvatarResult generatedAvatar = avatarImageGenerationService.generateAvatarImage(
                request.getPrompt(),
                request.getModelPreset(),
                referenceAvatarImage
        );

        String objectKey = null;
        try {
            objectKey = avatarImageStorageService.uploadGeneratedImage(
                    parsedUserId,
                    generatedAvatar.imageBytes(),
                    generatedAvatar.mimeType()
            );

            AvatarImage avatarImage = avatarImageRepository.save(
                    AvatarImage.builder()
                            .user(user)
                            .name(createAvatarName(request.getPrompt()))
                            .imageKey(objectKey)
                            .mimeType(generatedAvatar.mimeType())
                            .prompt(normalizePrompt(request.getPrompt()))
                            .generationModel(generatedAvatar.modelCode())
                            .sourceType(GENERATED_SOURCE_TYPE)
                            .referenceType(referenceAvatarImage == null ? DEFAULT_REFERENCE_TYPE : CUSTOM_REFERENCE_TYPE)
                            .referenceAvatarImageId(referenceAvatarImage == null ? null : referenceAvatarImage.getAvatarImageId())
                            .build()
            );

            user.setActiveAvatarImage(avatarImage);
            userRepository.save(user);

            List<AvatarImage> avatarImages = avatarImageRepository.findAllByUser_UserIdOrderByCreatedAtDesc(parsedUserId);
            return buildCollectionResponse(user, avatarImages);
        } catch (RuntimeException exception) {
            avatarImageStorageService.deleteQuietly(objectKey);
            throw exception;
        }
    }

    @Transactional
    public AvatarImageDto.CollectionResponse setActiveAvatarImage(String userId, AvatarImageDto.SetActiveRequest request) {
        UUID parsedUserId = UUID.fromString(userId);
        User user = getUserWithActiveAvatarImage(parsedUserId);

        if (request.getAvatarImageId() == null) {
            user.setActiveAvatarImage(null);
        } else {
            user.setActiveAvatarImage(getOwnedAvatarImage(parsedUserId, request.getAvatarImageId()));
        }

        userRepository.save(user);
        List<AvatarImage> avatarImages = avatarImageRepository.findAllByUser_UserIdOrderByCreatedAtDesc(parsedUserId);
        return buildCollectionResponse(user, avatarImages);
    }

    @Transactional
    public AvatarImageDto.CollectionResponse deleteAvatarImage(String userId, Long avatarImageId) {
        UUID parsedUserId = UUID.fromString(userId);
        User user = getUserWithActiveAvatarImage(parsedUserId);
        AvatarImage avatarImage = getOwnedAvatarImage(parsedUserId, avatarImageId);

        if (user.getActiveAvatarImage() != null
                && Objects.equals(user.getActiveAvatarImage().getAvatarImageId(), avatarImageId)) {
            user.setActiveAvatarImage(null);
            userRepository.save(user);
            userRepository.flush();
        }

        avatarImageStorageService.delete(avatarImage.getImageKey());
        avatarImageRepository.delete(avatarImage);

        List<AvatarImage> avatarImages = avatarImageRepository.findAllByUser_UserIdOrderByCreatedAtDesc(parsedUserId);
        return buildCollectionResponse(user, avatarImages);
    }

    private AvatarImageDto.CollectionResponse buildCollectionResponse(User user, List<AvatarImage> avatarImages) {
        Long activeAvatarImageId = user.getActiveAvatarImage() == null
                ? null
                : user.getActiveAvatarImage().getAvatarImageId();

        List<AvatarImageDto.ItemResponse> items = avatarImages.stream()
                .map(avatarImage -> AvatarImageDto.ItemResponse.from(
                        avatarImage,
                        avatarImageStorageService.createReadUrl(avatarImage.getImageKey()),
                        Objects.equals(activeAvatarImageId, avatarImage.getAvatarImageId())
                ))
                .toList();

        AvatarImageDto.ItemResponse activeAvatarImage = items.stream()
                .filter(AvatarImageDto.ItemResponse::isActive)
                .findFirst()
                .orElse(null);

        return AvatarImageDto.CollectionResponse.builder()
                .usesDefaultAvatar(activeAvatarImageId == null)
                .activeAvatarImage(activeAvatarImage)
                .avatarImages(items)
                .build();
    }

    private User getUserWithActiveAvatarImage(UUID userId) {
        return userRepository.findByUserIdWithActiveAvatarImage(userId)
                .orElseThrow(() -> new CustomException(ApiStatus.USER_NOT_FOUND));
    }

    private AvatarImage getOwnedAvatarImage(UUID userId, Long avatarImageId) {
        return avatarImageRepository.findByAvatarImageIdAndUser_UserId(avatarImageId, userId)
                .orElseThrow(() -> new CustomException(ApiStatus.AVATAR_IMAGE_NOT_FOUND));
    }

    private String createAvatarName(String prompt) {
        normalizePrompt(prompt);
        return "생성 캐릭터";
    }

    private String normalizePrompt(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new CustomException(ApiStatus.BAD_REQUEST, "Avatar generation prompt is required");
        }
        return prompt.trim().replaceAll("\\s+", " ");
    }
}
