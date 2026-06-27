package com.yourmicroworld.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByChapterIdAndStatusOrderByParagraphIndexAscCreatedAtAsc(Long chapterId, String status);
    long countByChapterNovelAuthorIdAndStatus(Long authorId, String status);
    Optional<Comment> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<Comment> findTop20ByChapterNovelAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, String status);
    List<Comment> findByChapterNovelAuthorIdAndStatusAndOwnerReadAtIsNull(Long authorId, String status);
    Optional<Comment> findByIdAndChapterNovelAuthorUsername(Long commentId, String username);

    @Query("select coalesce(sum(c.likeCount), 0) from Comment c where c.chapter.novel.author.id = :authorId and c.status = :status")
    long sumLikeCountByNovelAuthorIdAndStatus(@Param("authorId") Long authorId, @Param("status") String status);
}
