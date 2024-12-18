package com.worthyi.worthyi_backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.worthyi.worthyi_backend.model.entity.AdultActionInstance;
import com.worthyi.worthyi_backend.model.entity.ChildActionInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AdultActionDto {
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @JsonProperty("adultActionContent")
        private String adultActionContent;
        private Long childActionId;
        // 프론트엔드에서 보내준 감사 내용

        public AdultActionInstance toEntity(AdultActionDto.Request request) {
            log.info("toEntity Starts");
            return AdultActionInstance.builder()
                    .data(request.adultActionContent)
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private Long adultActionId;
        private Long childActionId;
        private String adultActionContent;

        public static AdultActionDto.Response fromEntity(AdultActionInstance entity) {
            return AdultActionDto.Response.builder()
                    .adultActionContent(entity.getData())
                    .adultActionId(entity.getAdultActionInstanceId())
                    .childActionId(entity.getChildActionInstance().getChildActionInstanceId())
                    .build();
        }
    }
}
