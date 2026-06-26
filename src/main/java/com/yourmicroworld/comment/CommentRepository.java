package com.yourmicroworld.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByChapterIdAndStatusOrderByParagraphIndexAscCreatedAtAsc(Long chapterId, String status);
}
