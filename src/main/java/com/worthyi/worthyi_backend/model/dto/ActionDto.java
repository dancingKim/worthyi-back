package com.worthyi.worthyi_backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.worthyi.worthyi_backend.model.entity.AdultActionInstance;
import com.worthyi.worthyi_backend.model.entity.ChildActionInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ActionDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @JsonProperty("childActionContent")
        private String childActionContent;
        // 프론트엔드에서 보내준 감사 내용

        public ChildActionInstance toEntity(ActionDto.Request actionDto) {
        log.info("toEntity Starts");
            return ChildActionInstance.builder()
                    .data(actionDto.childActionContent)
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private Long childActionId;
        private String childActionContent;
        private List<AdultActionDto.Response> adultActions;

        public static Response fromEntity(ChildActionInstance entity) {
            List<AdultActionDto.Response> adultActionResponses = null;
            if (entity.getAdultActionInstances() != null) {
                adultActionResponses = entity.getAdultActionInstances().stream()
                        .map(AdultActionDto.Response::fromEntity)
                        .collect(Collectors.toList());
            } else {
                adultActionResponses = new ArrayList<>(); // 빈 리스트로 초기화
            }

            return Response.builder()
                    .childActionId(entity.getChildActionInstanceId())
                    .childActionContent(entity.getData())
                    .adultActions(adultActionResponses)
                    .build();
        }
    }
}
