package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import com.worthyi.worthyi_backend.model.entity.AvatarImage;
import com.worthyi.worthyi_backend.model.entity.User;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserDto {
    
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        // 더 이상 email과 name을 쓰지 않는다면, 제거하거나 빈 값으로 두세요.
        private String email;
        private String name;

        private List<AvatarResponse> avatars;
        private Boolean usesDefaultAvatar;
        private ActiveAvatarImageResponse activeAvatarImage;
        
        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AvatarResponse {
            private Long avatarId;
            private String name;
            private String appearance;
            
            public static AvatarResponse from(Avatar avatar) {
                return AvatarResponse.builder()
                    .avatarId(avatar.getAvatarId())
                    .name(avatar.getName())
                    .appearance(avatar.getAppearance())
                    .build();
            }
        }

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActiveAvatarImageResponse {
            private Long avatarImageId;
            private String name;
            private String imageUrl;

            public static ActiveAvatarImageResponse from(AvatarImage avatarImage, String imageUrl) {
                if (avatarImage == null) {
                    return null;
                }

                return ActiveAvatarImageResponse.builder()
                        .avatarImageId(avatarImage.getAvatarImageId())
                        .name(avatarImage.getName())
                        .imageUrl(imageUrl)
                        .build();
            }
        }
        
        public static Response from(User user, String activeAvatarImageUrl) {
            return Response.builder()
                .email("")      // 더 이상 이메일을 안 쓰면 빈 문자열 또는 null
                .name("")       // 마찬가지
                .avatars(user.getAvatars() == null ? Collections.emptyList() : user.getAvatars().stream()
                    .map(AvatarResponse::from)
                    .collect(Collectors.toList()))
                .usesDefaultAvatar(user.getActiveAvatarImage() == null)
                .activeAvatarImage(ActiveAvatarImageResponse.from(user.getActiveAvatarImage(), activeAvatarImageUrl))
                .build();
        }
    }
}
