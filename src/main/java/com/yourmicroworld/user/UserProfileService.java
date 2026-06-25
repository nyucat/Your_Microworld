package com.yourmicroworld.user;

import com.yourmicroworld.chapter.ChapterRepository;
import com.yourmicroworld.novel.NovelRepository;
import com.yourmicroworld.novel.NovelSummary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;

    public UserProfileService(UserRepository userRepository, NovelRepository novelRepository, ChapterRepository chapterRepository) {
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt(),
                novelRepository.countByAuthorIdAndStatus(userId, "PUBLISHED"),
                chapterRepository.countByAuthorId(userId),
                novelRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(userId, "PUBLISHED", PageRequest.of(0, 6))
                        .stream()
                        .map(NovelSummary::from)
                        .toList()
        );
    }
}
