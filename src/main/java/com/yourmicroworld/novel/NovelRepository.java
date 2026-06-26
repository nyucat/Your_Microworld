package com.yourmicroworld.novel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelRepository extends JpaRepository<Novel, Long> {
    Page<Novel> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<Novel> findDistinctByStatusAndTagsNameOrderByCreatedAtDesc(String status, String tagName, Pageable pageable);
    Page<Novel> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, String status, Pageable pageable);
    long countByAuthorIdAndStatus(Long authorId, String status);
}
