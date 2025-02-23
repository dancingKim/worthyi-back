package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.model.dto.UserDto;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto.Response> getUserInfo(@AuthenticationPrincipal PrincipalDetails principal) {
        String userId = principal.getName();
        log.info("GET /user/me - Requesting user information for userId: {}", userId);
        
        UserDto.Response response = userService.getUserInfo(userId);
        
        log.info("GET /user/me - Successfully retrieved user information for userId: {}", userId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal PrincipalDetails principal) {
        String userId = principal.getName();
        log.info("DELETE /user/me - 사용자 삭제 요청. userId: {}", userId);
        
        userService.deleteUserAndRelatedData(UUID.fromString(userId));
        
        log.info("DELETE /user/me - 사용자 삭제 완료. userId: {}", userId);
        return ApiResponse.success(null);
    }
} 