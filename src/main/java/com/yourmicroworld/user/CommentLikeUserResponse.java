package com.yourmicroworld.user;

import java.time.Instant;

public record CommentLikeUserResponse(
        Long userId,
        String username,
        Instant likedAt
) {
}
