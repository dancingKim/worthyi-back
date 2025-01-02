package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.model.dto.UserDto;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto.Response> getUserInfo(@AuthenticationPrincipal PrincipalDetails principal) {
        Long userId = principal.getUser().getUserId();
        log.info("GET /user/me - Requesting user information for userId: {}", userId);
        
        UserDto.Response response = userService.getUserInfo(userId);
        
        log.info("GET /user/me - Successfully retrieved user information for userId: {}", userId);
        return ApiResponse.success(response);
    }
} 