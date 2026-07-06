package com.cooksync_server.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "instructions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instruction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Recipe recipe; //[cite: 88]

    @Column(name = "step_number", nullable = false)
    private int stepNumber; // המספר הסידורי של השלב [cite: 89]

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; //[//cite: 89]

    @Column(name = "has_timer", nullable = false)
    private boolean hasTimer = false;// [cite: 92]

    @Column(name = "time_seconds")
    private Integer timeSeconds; // משך הזמן בשניות, במידה ויש טיימר [cite: 92]

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;// [cite: 93]

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;// [cite: 93]

    // קישור למצרכים הרלוונטיים לשלב זה (טבלת גישור instruction_ingredients)
    @ManyToMany
    @JoinTable(
            name = "instruction_ingredients",
            joinColumns = @JoinColumn(name = "instruction_id"),// [cite: 97]
            inverseJoinColumns = @JoinColumn(name = "ingredient_id") // [cite: 98]
    )
    private Set<Ingredient> ingredients;

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
