package com.yourmicroworld.novel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovelRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String type,
        @Size(max = 5000) String description,
        @Size(max = 100000) String microContent,
        @Size(max = 20000) String worldSetting,
        @Size(max = 20000) String outlineContent,
        boolean allowIfBranch,
        boolean allowBid,
        @Size(max = 200) String firstChapterTitle,
        @Size(max = 100000) String firstChapterContent
) {}
