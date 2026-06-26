package com.yourmicroworld.novel;

import com.yourmicroworld.chapter.Chapter;
import com.yourmicroworld.chapter.ChapterRepository;
import com.yourmicroworld.chapter.ChapterRequest;
import com.yourmicroworld.chapter.ChapterResponse;
import com.yourmicroworld.tag.Tag;
import com.yourmicroworld.tag.TagRepository;
import com.yourmicroworld.user.AppUser;
import com.yourmicroworld.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NovelService {
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public NovelService(NovelRepository novelRepository, ChapterRepository chapterRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public NovelDetail create(String username, NovelRequest request) {
        AppUser author = user(username);
        NovelType type = parseType(request.type());

        validateCreateRequest(type, request);

        Novel novel = novelRepository.save(new Novel(
                request.title(),
                author,
                type,
                request.description(),
                normalizeCategory(request.category()),
                type == NovelType.MICRO ? clean(request.microContent()) : null,
                request.worldSetting(),
                request.outlineContent(),
                type == NovelType.SERIAL && request.allowIfBranch(),
                type == NovelType.SERIAL && request.allowBid()
        ));
        novel.getTags().addAll(resolveTags(request.tags()));

        if (type == NovelType.SERIAL) {
            chapterRepository.save(new Chapter(
                    novel,
                    null,
                    author,
                    clean(request.firstChapterTitle()),
                    clean(request.firstChapterContent()),
                    1
            ));
        } else {
            chapterRepository.save(new Chapter(
                    novel,
                    null,
                    author,
                    request.title(),
                    clean(request.microContent()),
                    1
            ));
        }

        return detail(novel);
    }

    @Transactional(readOnly = true)
    public Page<NovelSummary> list(int page, int size, String tag, String category) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        boolean noTag = blank(tag);
        boolean noCategory = blank(category);

        if (noTag && noCategory) {
            return novelRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED", pageable).map(NovelSummary::from);
        }

        if (noTag) {
            return novelRepository.findByStatusAndCategoryOrderByCreatedAtDesc("PUBLISHED", category.trim(), pageable)
                    .map(NovelSummary::from);
        }

        if (noCategory) {
            return novelRepository.findDistinctByStatusAndTagsNameOrderByCreatedAtDesc("PUBLISHED", tag.trim(), pageable)
                    .map(NovelSummary::from);
        }

        return novelRepository.findDistinctByStatusAndCategoryAndTagsNameOrderByCreatedAtDesc(
                        "PUBLISHED",
                        category.trim(),
                        tag.trim(),
                        pageable
                )
                .map(NovelSummary::from);
    }

    @Transactional
    public NovelDetail detail(Long id) {
        return detail(novel(id));
    }

    @Transactional
    public ChapterResponse addMainChapter(Long novelId, String username, ChapterRequest request) {
        Novel novel = novel(novelId);
        AppUser author = user(username);

        if (novel.getType() != NovelType.SERIAL) {
            throw new IllegalArgumentException("微小说不支持追加后续章节");
        }
        if (!novel.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("只有原作者可以在当前 Sprint 续写主线");
        }

        Chapter latest = chapterRepository.findTopByNovelIdAndTypeOrderBySequenceNoDesc(novelId, "MAIN")
                .orElseThrow(() -> new IllegalArgumentException("当前小说还没有可续写的主线章节"));

        Chapter saved = chapterRepository.save(new Chapter(
                novel,
                latest,
                author,
                request.title(),
                request.content(),
                latest.getSequenceNo() + 1
        ));
        return ChapterResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> chapters(Long novelId) {
        Novel novel = novel(novelId);
        if (novel.getType() != NovelType.SERIAL) {
            return List.of();
        }
        return chapterRepository.findByNovelIdAndTypeOrderBySequenceNoAsc(novelId, "MAIN")
                .stream()
                .map(ChapterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChapterResponse chapter(Long chapterId) {
        return ChapterResponse.from(
                chapterRepository.findById(chapterId)
                        .orElseThrow(() -> new IllegalArgumentException("章节不存在"))
        );
    }

    private NovelDetail detail(Novel novel) {
        Long microChapterId = novel.getType() == NovelType.MICRO
                ? ensureMicroChapter(novel).getId()
                : null;
        return new NovelDetail(
                novel.getId(),
                novel.getTitle(),
                novel.getType().name(),
                novel.getDescription(),
                novel.getCategory(),
                novel.getTags().stream().map(Tag::getName).sorted().toList(),
                novel.getMicroContent(),
                novel.getWorldSetting(),
                novel.getOutlineContent(),
                novel.getAuthor().getId(),
                novel.getAuthor().getUsername(),
                novel.isAllowIfBranch(),
                novel.isAllowBid(),
                microChapterId,
                novel.getCreatedAt(),
                chapters(novel.getId())
        );
    }

    private Chapter ensureMicroChapter(Novel novel) {
        return chapterRepository.findTopByNovelIdOrderBySequenceNoAsc(novel.getId())
                .orElseGet(() -> chapterRepository.save(new Chapter(
                        novel,
                        null,
                        novel.getAuthor(),
                        novel.getTitle(),
                        clean(novel.getMicroContent()),
                        1
                )));
    }

    private void validateCreateRequest(NovelType type, NovelRequest request) {
        if (type == NovelType.MICRO) {
            if (blank(request.microContent())) {
                throw new IllegalArgumentException("微小说必须填写正文");
            }
        } else if (blank(request.firstChapterTitle()) || blank(request.firstChapterContent())) {
            throw new IllegalArgumentException("连载小说必须填写第一章标题和正文");
        }

        if (blank(request.category())) {
            throw new IllegalArgumentException("请选择小说分类");
        }
    }

    private NovelType parseType(String rawType) {
        try {
            return NovelType.valueOf(rawType == null ? "" : rawType.trim().toUpperCase());
        } catch (Exception ignored) {
            throw new IllegalArgumentException("小说类型无效，只能是 MICRO 或 SERIAL");
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private java.util.List<Tag> resolveTags(java.util.List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) return java.util.List.of();

        return rawTags.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(tag -> tag.trim())
                .distinct()
                .limit(5)
                .map(tagName -> tagRepository.findByName(tagName).orElseGet(() -> tagRepository.save(new Tag(tagName))))
                .toList();
    }

    private String normalizeCategory(String rawCategory) {
        String category = clean(rawCategory);
        if (blank(category)) return null;
        if (!NovelCategories.ALL_SET.contains(category)) {
            throw new IllegalArgumentException("小说分类无效");
        }
        return category;
    }

    private Novel novel(Long id) {
        return novelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("小说不存在"));
    }

    private AppUser user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}
