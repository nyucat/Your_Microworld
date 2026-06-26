package com.yourmicroworld.novel;

import java.util.List;

public record CategoryOverviewResponse(
        String category,
        long totalNovels,
        long serialNovels,
        long microNovels,
        long authorCount,
        List<CategoryTagStat> popularTags
) {
}
