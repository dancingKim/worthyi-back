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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
@RestController
@RequestMapping("/action")
public class ActionController {
    private final ActionService actionService;

    @PostMapping("/child")
    public ResponseEntity<ApiResponse<ActionDto.Response>> saveChildAction(
            @RequestBody ActionDto.Request request,
            @AuthenticationPrincipal PrincipalDetails principal) {
        ApiResponse<ActionDto.Response> response = actionService.saveChildAction(request, principal);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/adult")
    public ApiResponse<AdultActionDto.Response> createAdultAction(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody AdultActionDto.Request actionDto
    ){
        log.info("principal: {}", principal);
        log.info("principal attribute: {}", principal.getAttributes());
        log.info("principal getName: {}", principal.getName());
        log.info("userId: {}", principal.getUser().getUserId());
        log.info("content: {}", actionDto.getContent());

        AdultActionDto.Response adultActionDto = actionService.saveAdultAction(actionDto, principal.getName());
        return ApiResponse.success(adultActionDto);
    }

    // ActionController.java
    @GetMapping
    public ApiResponse<List<ActionDto.Response>> getChildActionsByDate(
            @AuthenticationPrincipal PrincipalDetails principal,
               @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("getChildActionsByDate: {}", date);
        log.info("principal: {}", principal);
        log.info("userId: {}", principal.getUser().getUserId());
        Long userId = principal.getUser().getUserId();

        List<ActionDto.Response> responses = actionService.getChildActionsByDate(userId, date);
        return ApiResponse.success(responses);
    }


    @GetMapping("/logs")
    public ApiResponse<ActionDto.ActionLogResponse> getLogs(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        log.info("getLogs: {}", date);
        log.info("userId: {}", principal.getUser().getUserId());
        ActionDto.ActionLogResponse result = actionService.getActionLogs(principal, date);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/child/{id}")
    public ApiResponse<Void> deleteChildAction(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("Deleting child action: {}", id);
        actionService.deleteChildAction(id, principal.getUser().getUserId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/adult/{id}")
    public ApiResponse<Void> deleteAdultAction(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("Deleting adult action: {}", id);
        actionService.deleteAdultAction(id, principal.getUser().getUserId());
        return ApiResponse.success(null);
    }
}
