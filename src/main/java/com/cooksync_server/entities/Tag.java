package com.cooksync_server.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // [cite: 120]

    @Column(nullable = false, unique = true, length = 100)
    private String name; // [cite: 120]

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // [cite: 121]

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "recipe_tags", // 
            joinColumns = @JoinColumn(name = "tag_id"), // [cite: 125]
            inverseJoinColumns = @JoinColumn(name = "recipe_id") // [cite: 125]
    )
    @Builder.Default
    private Set<Recipe> recipes = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
