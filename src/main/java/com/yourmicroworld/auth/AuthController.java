package com.yourmicroworld.auth;

import com.yourmicroworld.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") public ApiResponse<AuthResponse> register(@Valid @RequestBody AuthRequest request) { return ApiResponse.ok(authService.register(request), "注册成功"); }
    @PostMapping("/login") public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) { return ApiResponse.ok(authService.login(request), "登录成功"); }
    @GetMapping("/me") public ApiResponse<String> me(Authentication authentication) { return ApiResponse.ok(authentication.getName()); }
}
