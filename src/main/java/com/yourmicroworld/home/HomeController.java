package com.yourmicroworld.home;

import com.yourmicroworld.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HomeController {
    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> health() { return ApiResponse.ok(Map.of("status", "UP", "service", "your-microworld")); }
    @GetMapping("/api/home")
    public ApiResponse<Map<String, String>> home() { return ApiResponse.ok(Map.of("message", "欢迎来到 Your Microworld。小说将在这里分叉、生长、合流。")); }
}
