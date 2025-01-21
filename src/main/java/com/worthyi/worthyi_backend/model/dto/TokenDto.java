package com.worthyi.worthyi_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

public class TokenDto {
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String authCode;
    }   

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String accessToken;
        private String refreshToken;

        public static Response of(String accessToken, String refreshToken) {
            return Response.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshRequest {
        private String refreshToken;

        public static RefreshRequest of(String refreshToken) {
            return RefreshRequest.builder()
                    .refreshToken(refreshToken)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshResponse {
        private String accessToken;

        public static RefreshResponse of(String accessToken) {
            return RefreshResponse.builder()
                    .accessToken(accessToken)
                    .build();
        }
    }
}
