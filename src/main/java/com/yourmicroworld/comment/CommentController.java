package com.yourmicroworld.comment;

import com.yourmicroworld.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/chapters/{chapterId}/comments")
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long chapterId, Authentication authentication) {
        return ApiResponse.ok(commentService.list(chapterId, authentication == null ? null : authentication.getName()));
    }

    @PostMapping("/api/chapters/{chapterId}/comments")
    public ApiResponse<CommentResponse> create(
            @PathVariable Long chapterId,
            Authentication authentication,
            @Valid @RequestBody CommentRequest request
    ) {
        return ApiResponse.ok(
                commentService.create(chapterId, authentication.getName(), request),
                "评论发布成功"
        );
    }

    @PostMapping("/api/comments/{commentId}/like")
    public ApiResponse<CommentResponse> toggleLike(@PathVariable Long commentId, Authentication authentication) {
        return ApiResponse.ok(
                commentService.toggleLike(commentId, authentication.getName()),
                "点赞状态已更新"
        );
    }
}
