package com.worthyi.worthyi_backend.model.dto;

import com.worthyi.worthyi_backend.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.worthyi.worthyi_backend.model.entity.ChildActionInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ActionDto {

    /**
     * 일자별 감사 기록 및 통계 정보를 담는 DTO
     * ActionLogResponse
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionLogResponse {
        private List<DailyLog> dailyLogs; // 일자별 감사 정보 목록
        private int weeklyCount;          // 최근 1주일 감사 일수
        private int monthlyCount;         // 최근 1개월 감사 일수
        private int yearlyCount;          // 최근 1년 감사 일수
    }

    /**
     * DailyLog: 특정 일자에 대한 감사 행동 및 칭찬 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyLog {
        private LocalDate date;                // 해당 날짜
        private List<Response> actions; // 해당 날짜에 발생한 ChildAction(아동 행동) 목록
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @JsonProperty("content")
        private ActionContentDto content;
        
        public ChildActionInstance toEntity(ActionDto.Request actionDto) {
            return ChildActionInstance.builder()
                    .data(JsonUtils.toJson(actionDto.content))
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private Long id;          // childActionId -> id로 통일
        private ActionContentDto content;
        private List<AdultActionDto.Response> responses;  // adultActions -> responses로 변경
        
        public static Response fromEntity(ChildActionInstance entity) {
            List<AdultActionDto.Response> adultResponses = entity.getAdultActionInstances() != null
                    ? entity.getAdultActionInstances().stream()
                        .map(AdultActionDto.Response::fromEntity)
                        .collect(Collectors.toList())
                    : new ArrayList<>();

            return Response.builder()
                    .id(entity.getChildActionInstanceId())
                    .content(JsonUtils.fromJson(entity.getData(), ActionContentDto.class))
                    .responses(adultResponses)
                    .build();
        }
    }
}