package com.yourmicroworld.novel;

import com.yourmicroworld.chapter.Chapter;
import com.yourmicroworld.chapter.ChapterRepository;
import com.yourmicroworld.chapter.ChapterRequest;
import com.yourmicroworld.chapter.ChapterResponse;
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

    public NovelService(NovelRepository novelRepository, ChapterRepository chapterRepository, UserRepository userRepository) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
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
                type == NovelType.MICRO ? clean(request.microContent()) : null,
                request.worldSetting(),
                request.outlineContent(),
                type == NovelType.SERIAL && request.allowIfBranch(),
                type == NovelType.SERIAL && request.allowBid()
        ));

        if (type == NovelType.SERIAL) {
            chapterRepository.save(new Chapter(
                    novel,
                    null,
                    author,
                    clean(request.firstChapterTitle()),
                    clean(request.firstChapterContent()),
                    1
            ));
        }

        return detail(novel);
    }

    @Transactional(readOnly = true)
    public Page<NovelSummary> list(int page, int size) {
        return novelRepository.findByStatusOrderByCreatedAtDesc(
                "PUBLISHED",
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50))
        ).map(NovelSummary::from);
    }

    @Transactional(readOnly = true)
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
        return new NovelDetail(
                novel.getId(),
                novel.getTitle(),
                novel.getType().name(),
                novel.getDescription(),
                novel.getMicroContent(),
                novel.getWorldSetting(),
                novel.getOutlineContent(),
                novel.getAuthor().getId(),
                novel.getAuthor().getUsername(),
                novel.isAllowIfBranch(),
                novel.isAllowBid(),
                novel.getCreatedAt(),
                chapters(novel.getId())
        );
    }

    private void validateCreateRequest(NovelType type, NovelRequest request) {
        if (type == NovelType.MICRO) {
            if (blank(request.microContent())) {
                throw new IllegalArgumentException("微小说必须填写正文");
            }
            return;
        }

        if (blank(request.firstChapterTitle()) || blank(request.firstChapterContent())) {
            throw new IllegalArgumentException("连载小说必须填写第一章标题和正文");
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

    private Novel novel(Long id) {
        return novelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("小说不存在"));
    }

    private AppUser user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}
