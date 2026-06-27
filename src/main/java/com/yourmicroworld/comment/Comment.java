package com.yourmicroworld.comment;

import com.yourmicroworld.chapter.Chapter;
import com.yourmicroworld.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "paragraph_index", nullable = false)
    private int paragraphIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(nullable = false, length = 20)
    private String status = "VISIBLE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "owner_read_at")
    private Instant ownerReadAt;

    protected Comment() {
    }

    public Comment(Chapter chapter, int paragraphIndex, AppUser user, Comment parentComment, String content) {
        this.chapter = chapter;
        this.paragraphIndex = paragraphIndex;
        this.user = user;
        this.parentComment = parentComment;
        this.content = content;
    }

    @PreUpdate
    void updateTime() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Chapter getChapter() { return chapter; }
    public int getParagraphIndex() { return paragraphIndex; }
    public AppUser getUser() { return user; }
    public Comment getParentComment() { return parentComment; }
    public String getContent() { return content; }
    public int getLikeCount() { return likeCount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getOwnerReadAt() { return ownerReadAt; }

    public void markOwnerRead() { ownerReadAt = Instant.now(); }
    public void markOwnerUnread() { ownerReadAt = null; }

    public void increaseLikeCount() {
        likeCount += 1;
    }

    public void decreaseLikeCount() {
        likeCount = Math.max(0, likeCount - 1);
    }

    public void markDeleted() {
        this.status = "DELETED";
    }
}
