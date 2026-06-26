package com.yourmicroworld.novel;

import com.yourmicroworld.chapter.ChapterResponse;

import java.time.Instant;
import java.util.List;

public record NovelDetail(
        Long id,
        String title,
        String type,
        String description,
        List<String> tags,
        String microContent,
        String worldSetting,
        String outlineContent,
        Long authorId,
        String authorName,
        boolean allowIfBranch,
        boolean allowBid,
        Long microChapterId,
        Instant createdAt,
        List<ChapterResponse> chapters
) {}
