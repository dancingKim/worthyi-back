package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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
    public ApiResponse<ActionDto.Response> createChildAction(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody ActionDto.Request actionDto) {
        // principal: 인증된 사용자의 식별자(JWT에서 추출된 userId 등)
        log.info("principal: {}", principal);
        log.info("childActionContent: {}", actionDto.getChildActionContent());
        log.info("userId: {}", principal.getUser().getUserId());

        /*
        PlaceTemplate에 대한 정보를 넘겨준다.
        ChildActionInstance에 대한 정보를 넘겨 준다.

         */

        ActionDto.Response actionResponse = actionService.saveChildAction(actionDto, principal);
        return ApiResponse.success(actionResponse);
    }

    @PostMapping("/adult")
    public ApiResponse<AdultActionDto.Response> createAdultAction(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody AdultActionDto.Request actionDto
    ){
        log.info("principal: {}", principal);
        log.info("principal attribute: {}",principal.getAttributes());
        log.info("principal getName: {}", principal.getName());
        log.info("userId: {}", principal.getUser().getUserId());
        log.info("childActionContent: {}", actionDto.getAdultActionContent());

        AdultActionDto.Response adultActionDto=  actionService.saveAdultAction(actionDto, principal.getName());

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
}
