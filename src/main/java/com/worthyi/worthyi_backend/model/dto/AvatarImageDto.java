package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.model.entity.AvatarImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class AvatarImageDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        private String prompt;
        private Long referenceAvatarImageId;
        private String modelPreset;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetActiveRequest {
        private Long avatarImageId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private Long avatarImageId;
        private String name;
        private String imageUrl;
        private String prompt;
        private String generationModel;
        private String sourceType;
        private String referenceType;
        private Long referenceAvatarImageId;
        private boolean active;
        private boolean deletable;

        public static ItemResponse from(AvatarImage avatarImage, String imageUrl, boolean active) {
            return ItemResponse.builder()
                    .avatarImageId(avatarImage.getAvatarImageId())
                    .name(avatarImage.getName())
                    .imageUrl(imageUrl)
                    .prompt(avatarImage.getPrompt())
                    .generationModel(avatarImage.getGenerationModel())
                    .sourceType(avatarImage.getSourceType())
                    .referenceType(avatarImage.getReferenceType())
                    .referenceAvatarImageId(avatarImage.getReferenceAvatarImageId())
                    .active(active)
                    .deletable(true)
                    .build();
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionResponse {
        private boolean usesDefaultAvatar;
        private ItemResponse activeAvatarImage;
        private List<ItemResponse> avatarImages;
    }
}
