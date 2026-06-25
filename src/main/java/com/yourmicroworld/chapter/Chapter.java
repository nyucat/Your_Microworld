package com.yourmicroworld.chapter;

import com.yourmicroworld.novel.Novel;
import com.yourmicroworld.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chapter")
public class Chapter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "novel_id", nullable = false) private Novel novel;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id") private Chapter parent;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false) private AppUser author;
    @Column(nullable = false, length = 20) private String type = "MAIN";
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "LONGTEXT") private String content;
    @Column(name = "sequence_no", nullable = false) private int sequenceNo;
    @Column(nullable = false, length = 20) private String status = "PUBLISHED";
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    protected Chapter() { }
    public Chapter(Novel novel, Chapter parent, AppUser author, String title, String content, int sequenceNo) {
        this.novel = novel; this.parent = parent; this.author = author; this.title = title; this.content = content; this.sequenceNo = sequenceNo;
    }
    public Long getId() { return id; } public Novel getNovel() { return novel; } public Chapter getParent() { return parent; }
    public AppUser getAuthor() { return author; } public String getTitle() { return title; } public String getContent() { return content; }
    public int getSequenceNo() { return sequenceNo; } public String getType() { return type; } public Instant getCreatedAt() { return createdAt; }
}
