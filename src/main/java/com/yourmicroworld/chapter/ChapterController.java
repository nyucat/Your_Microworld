package com.yourmicroworld.chapter;
import com.yourmicroworld.common.ApiResponse;
import com.yourmicroworld.novel.NovelService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/chapters")
public class ChapterController {
    private final NovelService service; public ChapterController(NovelService service) { this.service = service; }
    @GetMapping("/{id}") public ApiResponse<ChapterResponse> detail(@PathVariable Long id) { return ApiResponse.ok(service.chapter(id)); }
}
