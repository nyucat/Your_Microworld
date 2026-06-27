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
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.ok(service.list(page, size, tag, category));
    }

    @GetMapping("/category-overview")
    public ApiResponse<CategoryOverviewResponse> categoryOverview(@RequestParam(required = false) String category) {
        return ApiResponse.ok(service.categoryOverview(category));
    }

    @PostMapping
    public ApiResponse<NovelDetail> create(Authentication auth, @Valid @RequestBody NovelRequest request) {
        return ApiResponse.ok(service.create(auth.getName(), request), "小说发布成功");
    }

    @GetMapping("/{id:\\d+}")
    public ApiResponse<NovelDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PatchMapping("/{id:\\d+}")
    public ApiResponse<NovelDetail> update(@PathVariable Long id, Authentication auth, @Valid @RequestBody NovelUpdateRequest request) {
        return ApiResponse.ok(service.update(id, auth.getName(), request), "小说信息已更新");
    }

    @DeleteMapping("/{id:\\d+}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth.getName());
        return ApiResponse.ok(null, "小说已删除");
    }

    @GetMapping("/{id:\\d+}/chapters")
    public ApiResponse<List<ChapterResponse>> chapters(@PathVariable Long id) {
        return ApiResponse.ok(service.chapters(id));
    }

    @PostMapping("/{id:\\d+}/chapters")
    public ApiResponse<ChapterResponse> addChapter(@PathVariable Long id, Authentication auth, @Valid @RequestBody ChapterRequest request) {
        return ApiResponse.ok(service.addMainChapter(id, auth.getName(), request), "章节发布成功");
    }
}
