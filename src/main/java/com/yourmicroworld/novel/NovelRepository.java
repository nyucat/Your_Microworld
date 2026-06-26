package com.yourmicroworld.novel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NovelRepository extends JpaRepository<Novel, Long> {
    Page<Novel> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    List<Novel> findAllByStatusOrderByCreatedAtDesc(String status);
    Page<Novel> findByStatusAndCategoryOrderByCreatedAtDesc(String status, String category, Pageable pageable);
    List<Novel> findAllByStatusAndCategoryOrderByCreatedAtDesc(String status, String category);
    Page<Novel> findDistinctByStatusAndTagsNameOrderByCreatedAtDesc(String status, String tagName, Pageable pageable);
    Page<Novel> findDistinctByStatusAndCategoryAndTagsNameOrderByCreatedAtDesc(String status, String category, String tagName, Pageable pageable);
    Page<Novel> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, String status, Pageable pageable);
    long countByAuthorIdAndStatus(Long authorId, String status);
    long countByStatus(String status);
    long countByStatusAndCategory(String status, String category);
    long countByStatusAndType(String status, NovelType type);
    long countByStatusAndCategoryAndType(String status, String category, NovelType type);

    @Query("select count(distinct n.author.id) from Novel n where n.status = :status")
    long countDistinctAuthorsByStatus(@Param("status") String status);

    @Query("select count(distinct n.author.id) from Novel n where n.status = :status and n.category = :category")
    long countDistinctAuthorsByStatusAndCategory(@Param("status") String status, @Param("category") String category);

}
