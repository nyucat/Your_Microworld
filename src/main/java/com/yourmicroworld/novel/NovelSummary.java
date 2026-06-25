package com.yourmicroworld.novel;

import java.time.Instant;

public record NovelSummary(
        Long id,
        String title,
        String type,
        String description,
        Long authorId,
        String authorName,
        Instant createdAt
) {
    public static NovelSummary from(Novel novel) {
        return new NovelSummary(
                novel.getId(),
                novel.getTitle(),
                novel.getType().name(),
                novel.getDescription(),
                novel.getAuthor().getId(),
                novel.getAuthor().getUsername(),
                novel.getCreatedAt()
        );
    }
}
