package com.yourmicroworld.user;

import com.yourmicroworld.chapter.ChapterRepository;
import com.yourmicroworld.comment.Comment;
import com.yourmicroworld.comment.CommentLikeRepository;
import com.yourmicroworld.comment.CommentRepository;
import com.yourmicroworld.novel.Novel;
import com.yourmicroworld.novel.NovelRepository;
import com.yourmicroworld.novel.NovelSummary;
import com.yourmicroworld.novel.NovelType;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public UserProfileService(
            UserRepository userRepository,
            NovelRepository novelRepository,
            ChapterRepository chapterRepository,
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository
    ) {
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        var showcase = PageRequest.of(0, 6);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getBio(),
                user.getCreatedAt(),
                recentActiveAt(userId, user.getCreatedAt()),
                novelRepository.countByAuthorIdAndStatus(userId, "PUBLISHED"),
                novelRepository.countByAuthorIdAndStatusAndType(userId, "PUBLISHED", NovelType.SERIAL),
                novelRepository.countByAuthorIdAndStatusAndType(userId, "PUBLISHED", NovelType.MICRO),
                chapterRepository.countByAuthorId(userId),
                commentRepository.countByChapterNovelAuthorIdAndStatus(userId, "VISIBLE"),
                commentRepository.sumLikeCountByNovelAuthorIdAndStatus(userId, "VISIBLE"),
                novelRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(userId, "PUBLISHED", showcase)
                        .stream()
                        .map(NovelSummary::from)
                        .toList(),
                novelRepository.findByAuthorIdAndStatusAndTypeOrderByCreatedAtDesc(userId, "PUBLISHED", NovelType.SERIAL, showcase)
                        .stream()
                        .map(NovelSummary::from)
                        .toList(),
                novelRepository.findByAuthorIdAndStatusAndTypeOrderByCreatedAtDesc(userId, "PUBLISHED", NovelType.MICRO, showcase)
                        .stream()
                        .map(NovelSummary::from)
                        .toList()
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, UserProfileUpdateRequest request) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setBio(clean(request.bio()));
        return profile(user.getId());
    }

    @Transactional(readOnly = true)
    public UserInteractionResponse interactions(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<Comment> comments = commentRepository.findTop20ByChapterNovelAuthorIdAndStatusOrderByCreatedAtDesc(user.getId(), "VISIBLE");
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();

        Map<Long, List<CommentLikeUserResponse>> likedUsersMap = commentIds.isEmpty()
                ? Map.of()
                : commentLikeRepository.findLikedUsersByCommentIds(commentIds).stream()
                .collect(Collectors.groupingBy(
                        CommentLikeRepository.CommentLikeUserView::getCommentId,
                        Collectors.mapping(
                                row -> new CommentLikeUserResponse(row.getUserId(), row.getUsername(), row.getCreatedAt()),
                                Collectors.toList()
                        )
                ));

        return new UserInteractionResponse(
                comments.stream()
                        .map(comment -> new ReceivedCommentResponse(
                                comment.getId(),
                                comment.getChapter().getId(),
                                comment.getChapter().getNovel().getId(),
                                comment.getChapter().getNovel().getTitle(),
                                comment.getParagraphIndex(),
                                comment.getUser().getId(),
                                comment.getUser().getUsername(),
                                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                                comment.getParentComment() == null ? null : comment.getParentComment().getUser().getUsername(),
                                comment.getContent(),
                                comment.getLikeCount(),
                                comment.getCreatedAt(),
                                comment.getOwnerReadAt() != null,
                                likedUsersMap.getOrDefault(comment.getId(), List.of())
                        ))
                        .toList()
        );
    }

    @Transactional
    public UserInteractionResponse updateInteractionReadState(String username, Long commentId, InteractionReadRequest request) {
        Comment comment = commentRepository.findByIdAndChapterNovelAuthorUsername(commentId, username)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在或无权操作"));

        if (request.read()) {
            comment.markOwnerRead();
        } else {
            comment.markOwnerUnread();
        }

        return interactions(username);
    }

    @Transactional
    public UserInteractionResponse markAllInteractionsRead(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        commentRepository.findByChapterNovelAuthorIdAndStatusAndOwnerReadAtIsNull(user.getId(), "VISIBLE")
                .forEach(Comment::markOwnerRead);

        return interactions(username);
    }

    private Instant recentActiveAt(Long userId, Instant fallback) {
        Instant latestNovel = novelRepository.findTopByAuthorIdAndStatusOrderByCreatedAtDesc(userId, "PUBLISHED")
                .map(Novel::getCreatedAt)
                .orElse(null);
        Instant latestChapter = chapterRepository.findTopByAuthorIdOrderByCreatedAtDesc(userId)
                .map(chapter -> chapter.getCreatedAt())
                .orElse(null);
        Instant latestComment = commentRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(comment -> comment.getCreatedAt())
                .orElse(null);

        Instant latest = fallback;
        if (latestNovel != null && latestNovel.isAfter(latest)) latest = latestNovel;
        if (latestChapter != null && latestChapter.isAfter(latest)) latest = latestChapter;
        if (latestComment != null && latestComment.isAfter(latest)) latest = latestComment;
        return latest;
    }

    private String clean(String value) {
        if (value == null) return null;
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
