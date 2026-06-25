package com.yourmicroworld.chapter;
import java.time.Instant;
public record ChapterResponse(Long id, Long novelId, String novelTitle, Long authorId, String authorName, String title, String content, int sequenceNo, String type, Instant createdAt) {
    public static ChapterResponse from(Chapter chapter) { return new ChapterResponse(chapter.getId(), chapter.getNovel().getId(), chapter.getNovel().getTitle(), chapter.getAuthor().getId(), chapter.getAuthor().getUsername(), chapter.getTitle(), chapter.getContent(), chapter.getSequenceNo(), chapter.getType(), chapter.getCreatedAt()); }
}
