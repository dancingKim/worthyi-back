package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.worthyi.worthyi_backend.common.ApiStatus;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/action")
public class ActionController {
    private final ActionService actionService;

    @PostMapping("/child")
    public ResponseEntity<ApiResponse<ActionDto.Response>> saveChildAction(
            @RequestBody ActionDto.Request request,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Save Child Action Start ===");
        log.debug("Request payload: {}", request);
        log.debug("User: id={}, providerUserId={}", principal.getUser().getUserId(), principal.getUsername());
        
        ApiResponse<ActionDto.Response> response = actionService.saveChildAction(request, principal);
        
        log.info("=== Save Child Action End === Status: {}", response.getCode());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{childActionId}/adult")
    public ApiResponse<AdultActionDto.Response> saveAdultAction(
            @PathVariable Long childActionId,
            @RequestBody AdultActionDto.Request actionDto,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Save Adult Action Request Received ===");
        log.debug("Request details - ChildActionId: {}, Payload: {}, UserId: {}", 
            childActionId, actionDto, principal.getUser().getUserId());

        try {
            ApiResponse<AdultActionDto.Response> response = 
                actionService.saveAdultAction(childActionId, actionDto, principal);
            log.info("Save Adult Action completed with status: {}", response.getCode());
            return response;
        } catch (Exception e) {
            log.error("Failed to save adult action: {}", e.getMessage(), e);
            return ApiResponse.error(ApiStatus.ACTION_SAVE_FAILED);
        }
    }

    // ActionController.java
    @GetMapping
    public ApiResponse<List<ActionDto.Response>> getChildActionsByDate(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("=== Get Child Actions By Date Start ===");
        log.debug("Requested date: {}", date);
        log.debug("User: id={}, providerUserId={}", principal.getUser().getUserId(), principal.getUsername());
        
        List<ActionDto.Response> responses = actionService.getChildActionsByDate(principal.getUser().getUserId(), date);
        
        log.info("=== Get Child Actions By Date End === Found {} actions", responses.size());
        return ApiResponse.success(responses);
    }


    @GetMapping("/logs")
    public ApiResponse<ActionDto.ActionLogResponse> getLogs(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        log.info("=== Get Action Logs Start ===");
        log.debug("Requested date: {}", date);
        log.debug("User: id={}, providerUserId={}", principal.getUser().getUserId(), principal.getUsername());
        
        ActionDto.ActionLogResponse result = actionService.getActionLogs(principal, date);
        
        log.info("=== Get Action Logs End === Successfully retrieved logs");
        return ApiResponse.success(result);
    }

    @DeleteMapping("/child/{id}")
    public ApiResponse<Void> deleteChildAction(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Delete Child Action Start ===");
        log.debug("Action ID to delete: {}", id);
        log.debug("User: id={}, providerUserId={}", principal.getUser().getUserId(), principal.getUsername());
        
        actionService.deleteChildAction(id, principal.getUser().getUserId());
        
        log.info("=== Delete Child Action End === Successfully deleted");
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{childActionId}/adult/{adultActionId}")
    public ApiResponse<Void> deleteAdultAction(
            @PathVariable Long childActionId,
            @PathVariable Long adultActionId,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Delete Adult Action Request Received ===");
        log.debug("Request details - ChildActionId: {}, AdultActionId: {}, UserId: {}", 
            childActionId, adultActionId, principal.getUser().getUserId());

        try {
            ApiResponse<Void> response = actionService.deleteAdultAction(childActionId, adultActionId, principal.getUser().getUserId());
            log.info("Delete Adult Action completed with status: {}", response.getCode());
            return response;
        } catch (Exception e) {
            log.error("Failed to delete adult action: {}", e.getMessage(), e);
            return ApiResponse.error(ApiStatus.ACTION_DELETE_FAILED);
        }
    }
}
