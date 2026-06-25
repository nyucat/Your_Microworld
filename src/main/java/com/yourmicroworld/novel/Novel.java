package com.yourmicroworld.novel;

import com.yourmicroworld.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "novel")
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NovelType type = NovelType.SERIAL;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "micro_content", columnDefinition = "LONGTEXT")
    private String microContent;

    @Column(name = "world_setting", columnDefinition = "TEXT")
    private String worldSetting;

    @Column(name = "outline_content", columnDefinition = "TEXT")
    private String outlineContent;

    @Column(name = "allow_if_branch", nullable = false)
    private boolean allowIfBranch = true;

    @Column(name = "allow_bid", nullable = false)
    private boolean allowBid = true;

    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Novel() {}

    public Novel(
            String title,
            AppUser author,
            NovelType type,
            String description,
            String microContent,
            String worldSetting,
            String outlineContent,
            boolean allowIfBranch,
            boolean allowBid
    ) {
        this.title = title;
        this.author = author;
        this.type = type;
        this.description = description;
        this.microContent = microContent;
        this.worldSetting = worldSetting;
        this.outlineContent = outlineContent;
        this.allowIfBranch = allowIfBranch;
        this.allowBid = allowBid;
    }

    @PreUpdate
    void updateTime() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public AppUser getAuthor() { return author; }
    public NovelType getType() { return type; }
    public String getDescription() { return description; }
    public String getMicroContent() { return microContent; }
    public String getWorldSetting() { return worldSetting; }
    public String getOutlineContent() { return outlineContent; }
    public boolean isAllowIfBranch() { return allowIfBranch; }
    public boolean isAllowBid() { return allowBid; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
