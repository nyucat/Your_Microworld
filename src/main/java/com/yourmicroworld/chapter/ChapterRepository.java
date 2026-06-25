package com.yourmicroworld.chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByNovelIdAndTypeOrderBySequenceNoAsc(Long novelId, String type);
    Optional<Chapter> findTopByNovelIdAndTypeOrderBySequenceNoDesc(Long novelId, String type);
    long countByAuthorId(Long authorId);
}
