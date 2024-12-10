package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

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

        /*
        PlaceTemplate에 대한 정보를 넘겨준다.
        ChildActionInstance에 대한 정보를 넘겨 준다.

         */

        ActionDto.Response actionResponse = actionService.saveChildAction(actionDto, principal);
        return ApiResponse.success(actionResponse);
    }

    @PostMapping("/adult")
    public ApiResponse<AdultActionDto.Response> createAdultAction(
            @AuthenticationPrincipal User user,
            @RequestBody AdultActionDto.Request actionDto
    ){

        AdultActionDto.Response adultActionDto=  actionService.saveAdultAction(actionDto, user.getUsername());

        return ApiResponse.success(adultActionDto);
    }

}
