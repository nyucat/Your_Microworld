package com.yourmicroworld.chapter;

import com.yourmicroworld.common.ApiResponse;
import com.yourmicroworld.novel.NovelService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {
    private final NovelService service;

    public ChapterController(NovelService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ApiResponse<ChapterResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.chapter(id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<ChapterResponse> update(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody ChapterUpdateRequest request
    ) {
        return ApiResponse.ok(service.updateChapter(id, authentication.getName(), request), "章节已更新");
    }
}
