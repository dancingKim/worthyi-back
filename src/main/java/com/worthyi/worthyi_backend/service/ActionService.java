package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.entity.*;
import com.worthyi.worthyi_backend.repository.*;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActionService {
    private final PlaceInstanceRepository placeInstanceRepository;
    private final AvatarRepository avatarRepository;

    private final ChildActionTemplateRepository childActionTemplateRepository;
    private final AdultActionTemplateRepository adultActionTemplateRepository;
    private final ChildActionInstanceRepository childActionInstanceRepository;
    private final AdultActionInstanceRepository adultActionInstanceRepository;

    public ApiResponse<ActionDto.Response> saveChildAction(ActionDto.Request actionDto, PrincipalDetails user) {
        try {
            PlaceInstance placeInstance = placeInstanceRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException(ApiStatus.NOT_FOUND.getMessage()));
            ChildActionTemplate childActionTemplate = childActionTemplateRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));
            Avatar avatar = avatarRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Avatar not found"));

            // content를 JSON으로 변환하여 저장
            ChildActionInstance childActionInstance = actionDto.toEntity(actionDto);
            childActionInstance.setPlaceInstance(placeInstance);
            childActionInstance.setChildActionTemplate(childActionTemplate);
            childActionInstance.setAvatarId(avatar.getAvatarId());
            
            log.info("Saving action with content: {}", actionDto.getContent());
            ChildActionInstance instance = childActionInstanceRepository.save(childActionInstance);

            return ApiResponse.success(ActionDto.Response.fromEntity(instance));
        } catch (Exception e) {
            log.error("Failed to save child action", e);
            return ApiResponse.error(ApiStatus.INTERNAL_SERVER_ERROR, "액션 저장 중 오류가 발생했습니다");
        }
    }

    public AdultActionDto.Response saveAdultAction(AdultActionDto.Request actionDto, String email) {
        log.info("actionService Starts");
        AdultActionTemplate adultActionTemplate = adultActionTemplateRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));
        ChildActionInstance childActionInstance = childActionInstanceRepository.findById(actionDto.getChildActionId())
                .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));

        log.info("Creating adult action with content: {}", actionDto.getContent());
        AdultActionInstance adultActionInstance = actionDto.toEntity(actionDto);
        adultActionInstance.setChildActionInstance(childActionInstance);
        adultActionInstance.setAdultActionTemplate(adultActionTemplate);
        adultActionInstance.setUserId(1L);

        AdultActionInstance instance = adultActionInstanceRepository.save(adultActionInstance);

        return AdultActionDto.Response.fromEntity(instance);
    }

    // ActionService.java
    public List<ActionDto.Response> getChildActionsByDate(Long userId, LocalDate date) {
        // 유저 아이디를 이용해 유저의 아바타 목록을 조회
       Long avatarId =  avatarRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")).getAvatarId();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        log.info("startOfDay: {}", startOfDay);
        log.info("endOfDay: {}", endOfDay);

        List<ChildActionInstance> instances = childActionInstanceRepository.findAllByDateAndAvatarId(avatarId,startOfDay,endOfDay);

        instances.forEach(childActionInstance -> {
            log.info("createdAt: {}", childActionInstance.getCreatedAt());
        });


        return instances.stream()
                .map(ActionDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public ActionDto.ActionLogResponse getActionLogs(PrincipalDetails principal, LocalDate date) {
        log.info("getActionLogs 시작 - userId: {}, 요청 날짜: {}", principal.getUser().getUserId(), date);
        
        Long userId = principal.getUser().getUserId();
        Long avatarId = avatarRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")).getAvatarId();
        log.info("조회된 avatarId: {}", avatarId);

        // 오늘의 시작과 끝
        LocalDateTime todayStart = date.atStartOfDay();
        LocalDateTime todayEnd = date.plusDays(1).atStartOfDay();
        log.info("오늘 기간 - 시작: {}, 종료: {}", todayStart, todayEnd);

        // 이번 년도의 시작과 끝
        LocalDateTime yearStart = date.withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd = date.withDayOfYear(date.lengthOfYear()).plusDays(1).atStartOfDay();
        log.info("연간 기간 - 시작: {}, 종료: {}", yearStart, yearEnd);

        // 이번 주의 시작과 끝
        LocalDateTime weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();
        log.info("주간 기간 - 시작: {}, 종료: {}", weekStart, weekEnd);

        // 이번 달의 시작과 끝
        LocalDateTime monthStart = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = date.withDayOfMonth(date.lengthOfMonth()).plusDays(1).atStartOfDay();
        log.info("월간 기간 - 시작: {}, 종료: {}", monthStart, monthEnd);

        // 오늘의 감사 목록 조회
        List<ChildActionInstance> todayActions = childActionInstanceRepository.findAllByDateAndAvatarId(
                avatarId, todayStart, todayEnd);
        log.info("오늘의 감사 기록 수: {}", todayActions.size());
        
        List<ActionDto.DailyLog> dailyLogs = new ArrayList<>();
        dailyLogs.add(ActionDto.DailyLog.builder()
                .date(date)
                .actions(todayActions.stream()
                        .map(ActionDto.Response::fromEntity)
                        .collect(Collectors.toList()))
                .build());

        // 각 기간별 감사 일수 조회
        int weeklyCount = childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(
                avatarId, weekStart, weekEnd);
        int monthlyCount = childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(
                avatarId, monthStart, monthEnd); 
        int yearlyCount = childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(
                avatarId, yearStart, yearEnd);
        
        log.info("집계 결과 - 주간: {}, 월간: {}, 연간: {}", weeklyCount, monthlyCount, yearlyCount);

        ActionDto.ActionLogResponse response = ActionDto.ActionLogResponse.builder()
                .dailyLogs(dailyLogs)
                .weeklyCount(weeklyCount)
                .monthlyCount(monthlyCount)
                .yearlyCount(yearlyCount)
                .build();
                
        log.info("getActionLogs 종료");
        return response;
    }
}
