package com.yourmicroworld.user;

import com.yourmicroworld.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserProfileResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(userProfileService.profile(id));
    }

    @GetMapping("/me/interactions")
    public ApiResponse<UserInteractionResponse> interactions(Authentication authentication) {
        return ApiResponse.ok(userProfileService.interactions(authentication.getName()));
    }

    @PatchMapping("/me/interactions/{commentId}/read-state")
    public ApiResponse<UserInteractionResponse> updateReadState(
            Authentication authentication,
            @PathVariable Long commentId,
            @Valid @RequestBody InteractionReadRequest request
    ) {
        return ApiResponse.ok(userProfileService.updateInteractionReadState(authentication.getName(), commentId, request));
    }

    @PatchMapping("/me/interactions/read-all")
    public ApiResponse<UserInteractionResponse> markAllRead(Authentication authentication) {
        return ApiResponse.ok(userProfileService.markAllInteractionsRead(authentication.getName()));
    }

    @PatchMapping("/me/profile")
    public ApiResponse<UserProfileResponse> update(Authentication authentication, @Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.ok(userProfileService.updateProfile(authentication.getName(), request), "个人主页已更新");
    }
}
