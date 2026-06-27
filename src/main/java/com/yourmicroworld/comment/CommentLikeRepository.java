package com.yourmicroworld.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentLikeRepository extends JpaRepository<Comment, Long> {
    interface CommentLikeUserView {
        Long getCommentId();
        Long getUserId();
        String getUsername();
        java.time.Instant getCreatedAt();
    }

    @Query(value = "select count(*) from comment_like where comment_id = :commentId and user_id = :userId", nativeQuery = true)
    long countLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "insert ignore into comment_like(comment_id, user_id) values (:commentId, :userId)", nativeQuery = true)
    void insertLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "delete from comment_like where comment_id = :commentId and user_id = :userId", nativeQuery = true)
    void deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query(value = "select comment_id from comment_like where user_id = :userId and comment_id in (:commentIds)", nativeQuery = true)
    List<Long> findLikedCommentIds(@Param("commentIds") Collection<Long> commentIds, @Param("userId") Long userId);

    @Query(value = """
            select cl.comment_id as commentId, u.id as userId, u.username as username, cl.created_at as createdAt
            from comment_like cl
            join users u on u.id = cl.user_id
            where cl.comment_id in (:commentIds)
            order by cl.created_at desc
            """, nativeQuery = true)
    List<CommentLikeUserView> findLikedUsersByCommentIds(@Param("commentIds") Collection<Long> commentIds);
}
