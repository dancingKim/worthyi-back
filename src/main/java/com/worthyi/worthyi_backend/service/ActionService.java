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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

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
    private final UserRepository userRepository;
    private final VillageInstanceRepository villageInstanceRepository;

    public ApiResponse<ActionDto.Response> saveChildAction(ActionDto.Request actionDto, PrincipalDetails user) {
        log.info("=== Processing Child Action Save ===");
        try {
            String userId = user.getName();
            log.debug("Processing for userId: {}", userId);
            
            // 1. User -> Avatar 조회
            log.debug("Searching for avatar with userId: {}", userId);
            Avatar avatar = avatarRepository.findByUserUserId(UUID.fromString(userId))
                    .orElseThrow(() -> {
                        log.error("Avatar not found for userId: {}", userId);
                        return new RuntimeException(ApiStatus.AVATAR_NOT_FOUND.getMessage());
                    });
            log.debug("Found avatar: {}", avatar);
            
            // 2. VillageInstance 조회
            log.debug("Fetching village for userId: {}", userId);
            VillageInstance villageInstance = villageInstanceRepository.findByUserUserId(UUID.fromString(userId))
                    .orElseThrow(() -> {
                        log.error("Village not found for userId: {}", userId);
                        return new RuntimeException(ApiStatus.VILLAGE_NOT_FOUND.getMessage());
                    });
            log.debug("Village found: {}", villageInstance.getVillageId());
            
            // 3. VillageInstance -> PlaceInstance 조회
            PlaceInstance placeInstance = placeInstanceRepository.findByVillageInstance_VillageId(villageInstance.getVillageId())
                    .orElseThrow(() -> new RuntimeException(ApiStatus.PLACE_NOT_FOUND.getMessage()));

            // 4. ChildActionTemplate 조회
            ChildActionTemplate childActionTemplate = childActionTemplateRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException(ApiStatus.ACTION_TEMPLATE_NOT_FOUND.getMessage()));

            // 5. ChildActionInstance 생성 및 저장
            ChildActionInstance childActionInstance = actionDto.toEntity(actionDto);
            childActionInstance.setPlaceInstance(placeInstance);
            childActionInstance.setChildActionTemplate(childActionTemplate);
            childActionInstance.setAvatarId(avatar.getAvatarId());
            
            log.info("Saving action with content: {}", actionDto.getContent());
            ChildActionInstance savedInstance = childActionInstanceRepository.save(childActionInstance);

            log.info("=== Child Action Save Completed Successfully ===");
            return ApiResponse.success(ActionDto.Response.fromEntity(savedInstance));
        } catch (Exception e) {
            log.error("Failed to save child action", e);
            return ApiResponse.error(ApiStatus.ACTION_SAVE_FAILED);
        }
    }

    @Transactional
    public ApiResponse<AdultActionDto.Response> saveAdultAction(
            Long childActionId, AdultActionDto.Request actionDto, PrincipalDetails principal) {
        log.info("=== Processing Adult Action Save ===");
        try {
            String userId = principal.getName();
            log.debug("Processing adult action - ChildActionId: {}, UserId: {}", childActionId, userId);
            
            // 1. ChildAction 존재 여부 확인
            ChildActionInstance childActionInstance = childActionInstanceRepository.findById(childActionId)
                    .orElseThrow(() -> {
                        log.error("Child action not found with id: {}", childActionId);
                        return new RuntimeException(ApiStatus.CHILD_ACTION_NOT_FOUND.getMessage());
                    });
            log.debug("Found child action: {}", childActionInstance);

            // 2. AdultActionTemplate 조회
            AdultActionTemplate adultActionTemplate = adultActionTemplateRepository.findById(1L)
                    .orElseThrow(() -> {
                        log.error("Adult action template not found with id: 1");
                        return new RuntimeException(ApiStatus.ACTION_TEMPLATE_NOT_FOUND.getMessage());
                    });
            log.debug("Found adult action template: {}", adultActionTemplate);

            // 3. AdultActionInstance 생성 및 연관관계 설정
            AdultActionInstance adultActionInstance = actionDto.toEntity(actionDto);
            adultActionInstance.setChildActionInstance(childActionInstance);
            adultActionInstance.setAdultActionTemplate(adultActionTemplate);
            adultActionInstance.setUserId(UUID.fromString(userId));
            log.debug("Created adult action instance: {}", adultActionInstance);

            // 4. 저장
            AdultActionInstance savedInstance = adultActionInstanceRepository.save(adultActionInstance);
            log.info("Adult action saved successfully - ChildActionId: {}, AdultActionId: {}", 
                childActionId, savedInstance.getAdultActionInstanceId());
            
            return ApiResponse.success(AdultActionDto.Response.fromEntity(savedInstance));
        } catch (Exception e) {
            log.error("Failed to save adult action for childActionId: {}", childActionId, e);
            return ApiResponse.error(ApiStatus.ACTION_SAVE_FAILED);
        }
    }

    // ActionService.java
    public List<ActionDto.Response> getChildActionsByDate(String userId, LocalDate date) {
        log.info("=== Fetching Child Actions By Date ===");
        log.debug("UserId: {}, Date: {}", userId, date);
        
        try {
            Avatar avatar = avatarRepository.findByUserUserId(UUID.fromString(userId))
                    .orElseThrow(() -> {
                        log.error("Avatar not found for userId: {}", userId);
                        return new RuntimeException(ApiStatus.AVATAR_NOT_FOUND.getMessage());
                    });
            log.debug("Avatar found: {}", avatar.getAvatarId());

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            log.debug("Time range: {} to {}", startOfDay, endOfDay);

            List<ChildActionInstance> instances = childActionInstanceRepository
                    .findAllByDateAndAvatarId(avatar.getAvatarId(), startOfDay, endOfDay);
            log.info("Found {} actions for date {}", instances.size(), date);

            return instances.stream()
                    .map(ActionDto.Response::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching child actions by date", e);
            throw e;
        }
    }

    public ActionDto.ActionLogResponse getActionLogs(PrincipalDetails principal, LocalDate date) {
        String userId = principal.getName();
        
        // 1. Avatar 조회
        Avatar avatar = avatarRepository.findByUserUserId(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Avatar not found"));

        // 나머지 로직...
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate monthStart = date.withDayOfMonth(1);
        LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());
        LocalDate yearStart = date.withDayOfYear(1);
        LocalDate yearEnd = date.withDayOfYear(date.lengthOfYear());

        List<ActionDto.DailyLog> dailyLogs = getChildActionsByDate(userId, date).stream()
                .map(action -> ActionDto.DailyLog.builder()
                        .date(date)
                        .actions(Collections.singletonList(action))
                        .build())
                .collect(Collectors.toList());

        // 통계 계산
        int weeklyCount = childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(
                avatar.getAvatarId(), 
                weekStart.atStartOfDay(),  // LocalDate -> LocalDateTime
                weekEnd.atStartOfDay()
        );
        int monthlyCount = childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(
                avatar.getAvatarId(), 
                monthStart.atStartOfDay(), 
                monthEnd.atStartOfDay()
        );
        int yearlyCount = childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(
                avatar.getAvatarId(), 
                yearStart.atStartOfDay(), 
                yearEnd.atStartOfDay()
        );

        return ActionDto.ActionLogResponse.builder()
                .dailyLogs(dailyLogs)
                .weeklyCount(weeklyCount)
                .monthlyCount(monthlyCount)
                .yearlyCount(yearlyCount)
                .build();
    }

    @Transactional
    public ApiResponse<Void> deleteChildAction(Long childActionId, String userId) {
        try {
            // 1. Avatar 조회
            Avatar avatar = avatarRepository.findByUserUserId(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException(ApiStatus.AVATAR_NOT_FOUND.getMessage()));

            // 2. ChildAction 조회 및 권한 확인
            ChildActionInstance childAction = childActionInstanceRepository.findById(childActionId)
                    .orElseThrow(() -> new RuntimeException(ApiStatus.CHILD_ACTION_NOT_FOUND.getMessage()));

            if (!childAction.getAvatarId().equals(avatar.getAvatarId())) {
                return ApiResponse.error(ApiStatus.NOT_AUTHORIZED_TO_DELETE);
            }

            childActionInstanceRepository.delete(childAction);
            log.info("Child action deleted: {}", childActionId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("Failed to delete child action", e);
            return ApiResponse.error(ApiStatus.ACTION_DELETE_FAILED);
        }
    }

    @Transactional
    public ApiResponse<Void> deleteAdultAction(Long childActionId, Long adultActionId, String userId) {
        log.info("=== Processing Adult Action Delete Request ===");
        log.debug("Delete request - ChildActionId: {}, AdultActionId: {}, UserId: {}", 
            childActionId, adultActionId, userId);

        try {
            // 1. ChildAction 존재 여부 확인
            ChildActionInstance childAction = childActionInstanceRepository.findById(childActionId)
                    .orElseThrow(() -> {
                        log.error("Child action not found with id: {}", childActionId);
                        return new RuntimeException(ApiStatus.CHILD_ACTION_NOT_FOUND.getMessage());
                    });
            log.debug("Found child action: {}", childAction);

            // 2. AdultAction 조회
            AdultActionInstance adultAction = adultActionInstanceRepository.findById(adultActionId)
                    .orElseThrow(() -> {
                        log.error("Adult action not found with id: {}", adultActionId);
                        return new RuntimeException(ApiStatus.ADULT_ACTION_NOT_FOUND.getMessage());
                    });
            log.debug("Found adult action: {}", adultAction);

            // 3. ChildAction과 AdultAction의 연관관계 확인
            if (!adultAction.getChildActionInstance().getChildActionInstanceId().equals(childActionId)) {
                log.error("Adult action {} does not belong to child action {}", adultActionId, childActionId);
                return ApiResponse.error(ApiStatus.INVALID_ACTION_RELATIONSHIP);
            }

            // 4. 권한 확인
            if (!adultAction.getUserId().equals(UUID.fromString(userId))) {
                log.warn("User {} not authorized to delete adult action {}", userId, adultActionId);
                return ApiResponse.error(ApiStatus.NOT_AUTHORIZED_TO_DELETE);
            }

            // 5. 삭제 수행
            adultActionInstanceRepository.delete(adultAction);
            log.info("Adult action deleted successfully - ChildActionId: {}, AdultActionId: {}", 
                childActionId, adultActionId);
            
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("Failed to delete adult action - ChildActionId: {}, AdultActionId: {}", 
                childActionId, adultActionId, e);
            return ApiResponse.error(ApiStatus.ACTION_DELETE_FAILED);
        }
    }
}
