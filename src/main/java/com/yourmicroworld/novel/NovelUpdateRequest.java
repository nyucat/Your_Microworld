package com.yourmicroworld.novel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NovelUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @Size(max = 40) String category,
        List<@Size(max = 40) String> tags,
        @Size(max = 100000) String microContent,
        @Size(max = 20000) String worldSetting,
        @Size(max = 20000) String outlineContent,
        boolean allowIfBranch,
        boolean allowBid
) {
}
