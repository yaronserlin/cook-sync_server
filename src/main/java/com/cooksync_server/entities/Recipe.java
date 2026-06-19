package com.cooksync_server.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // [cite: 64]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy; // [cite: 65]

    @Column(nullable = false, length = 255)
    private String title; // [cite: 68]

    @Column(columnDefinition = "TEXT")
    private String description; // [cite: 69]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty; // 

    @Column(name = "prep_time_minutes", nullable = false)
    private int prepTimeMinutes; // [cite: 71]

    @Column(name = "cook_time_minutes", nullable = false)
    private int cookTimeMinutes; // [cite: 72]

    @Column(nullable = false)
    private int servings; // [cite: 73]

    @Builder.Default
    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0; // [cite: 73]

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // [cite: 73]

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // [cite: 74]

    @ManyToMany
    @JoinTable(
            name = "recipe_tags",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD // 
    }
}
