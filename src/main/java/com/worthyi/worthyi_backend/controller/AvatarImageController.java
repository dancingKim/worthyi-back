package com.worthyi.worthyi_backend.controller;

import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.model.dto.AvatarImageDto;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import com.worthyi.worthyi_backend.service.AvatarImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/avatar-images")
public class AvatarImageController {

    private final AvatarImageService avatarImageService;

    @GetMapping
    public ApiResponse<AvatarImageDto.CollectionResponse> getAvatarImages(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        return ApiResponse.success(avatarImageService.getAvatarImages(principal.getName()));
    }

    @PostMapping("/generate")
    public ApiResponse<AvatarImageDto.CollectionResponse> generateAvatarImage(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody AvatarImageDto.GenerateRequest request
    ) {
        return ApiResponse.success(avatarImageService.generateAvatarImage(principal.getName(), request));
    }

    @PatchMapping("/active")
    public ApiResponse<AvatarImageDto.CollectionResponse> setActiveAvatarImage(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody AvatarImageDto.SetActiveRequest request
    ) {
        return ApiResponse.success(avatarImageService.setActiveAvatarImage(principal.getName(), request));
    }

    @DeleteMapping("/{avatarImageId}")
    public ApiResponse<AvatarImageDto.CollectionResponse> deleteAvatarImage(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long avatarImageId
    ) {
        return ApiResponse.success(avatarImageService.deleteAvatarImage(principal.getName(), avatarImageId));
    }
}
