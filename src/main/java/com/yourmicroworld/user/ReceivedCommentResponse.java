package com.yourmicroworld.user;

import java.time.Instant;
import java.util.List;

public record ReceivedCommentResponse(
        Long id,
        Long chapterId,
        Long novelId,
        String novelTitle,
        int paragraphIndex,
        Long userId,
        String username,
        Long parentCommentId,
        String parentUsername,
        String content,
        int likeCount,
        Instant createdAt,
        boolean readByAuthor,
        List<CommentLikeUserResponse> likedUsers
) {
}
