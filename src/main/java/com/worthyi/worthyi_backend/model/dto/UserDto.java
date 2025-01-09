package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import com.worthyi.worthyi_backend.model.entity.User;
import lombok.*;

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
        
        public static Response from(User user) {
            return Response.builder()
                .email("")      // 더 이상 이메일을 안 쓰면 빈 문자열 또는 null
                .name("")       // 마찬가지
                .avatars(user.getAvatars().stream()
                    .map(AvatarResponse::from)
                    .collect(Collectors.toList()))
                .build();
        }
    }
}