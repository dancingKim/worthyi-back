package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.ActionService;
import com.worthyi.worthyi_backend.common.ApiStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/action")
public class ActionController {
    private final ActionService actionService;

    @PostMapping("/child")
    public ApiResponse<ActionDto.Response> saveChildAction(
            @RequestBody ActionDto.Request request,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletRequest httpServletRequest) {
        log.info("=== Save Child Action Start ===");
        log.debug("Request payload: {}", request);
        log.debug("User: id={}, providerUserId={}", principal.getName(), principal.getUsername());
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        

        Optional<Cookie> refreshTokenCookie = Optional.ofNullable(httpServletRequest.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst());

        if (refreshTokenCookie.isPresent()) {
            log.info("Refresh token found in cookies: {}", refreshTokenCookie.get().getValue());
        } else {
            log.info("No refresh token found in cookies");
        }

        ApiResponse<ActionDto.Response> response = actionService.saveChildAction(request, principal);

        log.info("=== Save Child Action End === Status: {}", response.getCode());

        return response;
    }

    @PostMapping("/{childActionId}/adult")
    public ApiResponse<AdultActionDto.Response> saveAdultAction(
            @PathVariable Long childActionId,
            @RequestBody AdultActionDto.Request actionDto,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Save Adult Action Request Received ===");

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

    @GetMapping
    public ApiResponse<List<ActionDto.Response>> getChildActionsByDate(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("=== Get Child Actions By Date Start ===");
        log.debug("Requested date: {}", date);

        List<ActionDto.Response> responses = actionService.getChildActionsByDate(principal.getName(), date);

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

        ActionDto.ActionLogResponse result = actionService.getActionLogs(principal, date);

        log.info("=== Get Action Logs End === Successfully retrieved logs");
        return ApiResponse.success(result);
    }

    @DeleteMapping("/child/{id}")
    public ApiResponse<Void> deleteChildAction(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Delete Child Action Start ===");
        log.debug("User: id={}, providerUserId={}", principal.getName(), principal.getUsername());

        actionService.deleteChildAction(id, principal.getName());

        log.info("=== Delete Child Action End === Successfully deleted");
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{childActionId}/adult/{adultActionId}")
    public ApiResponse<Void> deleteAdultAction(
            @PathVariable Long childActionId,
            @PathVariable Long adultActionId,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("=== Delete Adult Action Request Received ===");

        try {
            ApiResponse<Void> response = actionService.deleteAdultAction(childActionId, adultActionId, principal.getName());
            log.info("Delete Adult Action completed with status: {}", response.getCode());
            return response;
        } catch (Exception e) {
            log.error("Failed to delete adult action: {}", e.getMessage(), e);
            return ApiResponse.error(ApiStatus.ACTION_DELETE_FAILED);
        }
    }
}