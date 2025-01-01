package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.worthyi.worthyi_backend.model.entity.AdultActionInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdultActionDto {
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @JsonProperty("content")
        private ActionContentDto content;
        private Long childActionId;
        // 프론트엔드에서 보내준 감사 내용

        public AdultActionInstance toEntity(AdultActionDto.Request request) {
            return AdultActionInstance.builder()
                    .data(JsonUtils.toJson(request.content))
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private Long id;
        private Long childActionId;
        private ActionContentDto content;

        public static AdultActionDto.Response fromEntity(AdultActionInstance entity) {
            return Response.builder()
                    .id(entity.getAdultActionInstanceId())
                    .childActionId(entity.getChildActionInstance().getChildActionInstanceId())
                    .content(JsonUtils.fromJson(entity.getData(), ActionContentDto.class))
                    .build();
        }
    }
}
