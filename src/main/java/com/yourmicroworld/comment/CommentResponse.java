package com.yourmicroworld.comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long chapterId,
        int paragraphIndex,
        Long userId,
        String username,
        Long parentCommentId,
        String parentUsername,
        String content,
        int likeCount,
        boolean likedByCurrentUser,
        Instant createdAt
) {
    public static CommentResponse from(Comment comment, boolean likedByCurrentUser) {
        return new CommentResponse(
                comment.getId(),
                comment.getChapter().getId(),
                comment.getParagraphIndex(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                comment.getParentComment() == null ? null : comment.getParentComment().getUser().getUsername(),
                comment.getContent(),
                comment.getLikeCount(),
                likedByCurrentUser,
                comment.getCreatedAt()
        );
    }
}
