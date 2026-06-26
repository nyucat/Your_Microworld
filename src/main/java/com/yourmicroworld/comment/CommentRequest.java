package com.yourmicroworld.comment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @Min(0) int paragraphIndex,
        Long parentCommentId,
        @NotBlank @Size(max = 5000) String content
) {}
