package com.yourmicroworld.user;

import com.yourmicroworld.novel.NovelSummary;

import java.time.Instant;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String username,
        String role,
        Instant joinedAt,
        long publishedNovelCount,
        long publishedChapterCount,
        List<NovelSummary> recentNovels
) { }
