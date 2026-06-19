package com.cooksync_server.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // תואם ל-char(36) במסד הנתונים
    private String id; // [cite: 59]

    @Column(nullable = false, length = 255)
    private String name; // [cite: 60]

    @Column(nullable = false, unique = true, length = 255)
    private String email; // [cite: 134]

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // [cite: 60]

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false; // [cite: 61]

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // [cite: 61]

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // [cite: 62]

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}