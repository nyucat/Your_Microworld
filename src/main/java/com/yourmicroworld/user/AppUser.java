package com.yourmicroworld.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_user_username", columnNames = "username"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32) private String username;
    @Column(nullable = false, length = 100) private String passwordHash;
    @Column(nullable = false, length = 20) private String role = "READER";
    @Column(columnDefinition = "TEXT") private String bio;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();

    protected AppUser() { }
    public AppUser(String username, String passwordHash) { this.username = username; this.passwordHash = passwordHash; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getBio() { return bio; }
    public Instant getCreatedAt() { return createdAt; }
    public void setBio(String bio) { this.bio = bio; }
}
