package com.yourmicroworld.comment;

import com.yourmicroworld.chapter.Chapter;
import com.yourmicroworld.chapter.ChapterRepository;
import com.yourmicroworld.user.AppUser;
import com.yourmicroworld.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository,
            ChapterRepository chapterRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long chapterId, String username) {
        chapter(chapterId);
        List<Comment> comments = commentRepository.findByChapterIdAndStatusOrderByParagraphIndexAscCreatedAtAsc(chapterId, "VISIBLE");
        Set<Long> likedCommentIds = likedCommentIds(comments, username);
        return comments.stream()
                .map(comment -> CommentResponse.from(comment, likedCommentIds.contains(comment.getId())))
                .toList();
    }

    @Transactional
    public CommentResponse create(Long chapterId, String username, CommentRequest request) {
        Chapter chapter = chapter(chapterId);
        AppUser user = user(username);
        validateParagraphIndex(chapter, request.paragraphIndex());

        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            parentComment = visibleComment(request.parentCommentId());
            if (!parentComment.getChapter().getId().equals(chapterId)) {
                throw new IllegalArgumentException("回复目标不在当前章节");
            }
            if (parentComment.getParagraphIndex() != request.paragraphIndex()) {
                throw new IllegalArgumentException("回复评论必须位于同一段落");
            }
        }

        Comment saved = commentRepository.save(
                new Comment(chapter, request.paragraphIndex(), user, parentComment, request.content().trim())
        );
        return CommentResponse.from(saved, false);
    }

    @Transactional
    public CommentResponse toggleLike(Long commentId, String username) {
        Comment comment = visibleComment(commentId);
        AppUser user = user(username);

        boolean liked;
        if (commentLikeRepository.countLike(commentId, user.getId()) > 0) {
            commentLikeRepository.deleteLike(commentId, user.getId());
            comment.decreaseLikeCount();
            liked = false;
        } else {
            commentLikeRepository.insertLike(commentId, user.getId());
            comment.increaseLikeCount();
            liked = true;
        }

        return CommentResponse.from(comment, liked);
    }

    @Transactional
    public void delete(Long commentId, String username) {
        Comment comment = visibleComment(commentId);
        AppUser user = user(username);

        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isNovelOwner = comment.getChapter().getNovel().getAuthor().getId().equals(user.getId());
        if (!isCommentOwner && !isNovelOwner) {
            throw new IllegalArgumentException("无权删除这条评论");
        }

        comment.markDeleted();
    }

    private void validateParagraphIndex(Chapter chapter, int paragraphIndex) {
        String[] paragraphs = chapter.getContent().split("\\n\\s*\\n|\\r?\\n");
        int paragraphCount = Math.max(paragraphs.length, 1);
        if (paragraphIndex < 0 || paragraphIndex >= paragraphCount) {
            throw new IllegalArgumentException("段落编号超出范围");
        }
    }

    private Chapter chapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
        if (!"PUBLISHED".equals(chapter.getStatus()) || !"PUBLISHED".equals(chapter.getNovel().getStatus())) {
            throw new IllegalArgumentException("章节不存在");
        }
        return chapter;
    }

    private Comment visibleComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (!"VISIBLE".equals(comment.getStatus())) {
            throw new IllegalArgumentException("评论不存在");
        }
        return comment;
    }

    private AppUser user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private Set<Long> likedCommentIds(List<Comment> comments, String username) {
        if (username == null || username.isBlank() || comments.isEmpty()) {
            return Set.of();
        }

        Long userId = user(username).getId();
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        return Set.copyOf(commentLikeRepository.findLikedCommentIds(commentIds, userId));
    }
}
