package com.yourmicroworld.user;

import com.yourmicroworld.novel.NovelSummary;

import java.time.Instant;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String username,
        String role,
        String bio,
        Instant joinedAt,
        Instant recentActiveAt,
        long publishedNovelCount,
        long serialNovelCount,
        long microNovelCount,
        long publishedChapterCount,
        long receivedCommentCount,
        long receivedLikeCount,
        List<NovelSummary> recentNovels,
        List<NovelSummary> serialNovels,
        List<NovelSummary> microNovels
) { }
