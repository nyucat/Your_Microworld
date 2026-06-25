package com.yourmicroworld.novel;
import com.yourmicroworld.chapter.ChapterResponse;
import java.time.Instant;
import java.util.List;
public record NovelDetail(Long id, String title, String description, String worldSetting, String outlineContent, Long authorId, String authorName, boolean allowIfBranch, boolean allowBid, Instant createdAt, List<ChapterResponse> chapters) { }
