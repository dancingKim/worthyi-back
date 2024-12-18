package com.worthyi.worthyi_backend.model.dto;

import lombok.*;

public class UserDto {
    
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long userId;
        private String email;
        private String name;
        private String imageUrl;
    }
}