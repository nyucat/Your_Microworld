package com.yourmicroworld.novel;

import com.yourmicroworld.chapter.ChapterRequest;
import com.yourmicroworld.chapter.ChapterResponse;
import com.yourmicroworld.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/novels")
public class NovelController {
    private final NovelService service;

    public NovelController(NovelService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<NovelSummary>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String tag
    ) {
        return ApiResponse.ok(service.list(page, size, tag));
    }

    @PostMapping
    public ApiResponse<NovelDetail> create(Authentication auth, @Valid @RequestBody NovelRequest request) {
        return ApiResponse.ok(service.create(auth.getName(), request), "小说发布成功");
    }

    @GetMapping("/{id}")
    public ApiResponse<NovelDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @GetMapping("/{id}/chapters")
    public ApiResponse<List<ChapterResponse>> chapters(@PathVariable Long id) {
        return ApiResponse.ok(service.chapters(id));
    }

    @PostMapping("/{id}/chapters")
    public ApiResponse<ChapterResponse> addChapter(@PathVariable Long id, Authentication auth, @Valid @RequestBody ChapterRequest request) {
        return ApiResponse.ok(service.addMainChapter(id, auth.getName(), request), "章节发布成功");
    }
}
