package com.yourmicroworld.novel;

import java.util.List;
import java.util.Set;

public final class NovelCategories {
    public static final List<String> ALL = List.of(
            "奇幻", "科幻", "校园", "都市", "悬疑", "古风", "治愈", "冒险", "其他"
    );

    public static final Set<String> ALL_SET = Set.copyOf(ALL);

    private NovelCategories() {}
}
