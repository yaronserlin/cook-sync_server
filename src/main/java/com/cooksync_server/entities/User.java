package com.cooksync_server.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity representing a registered user within the CookSync system. Maps to
 * the "users" table in the underlying relational database and utilizes Lombok
 * for boilerplate code reduction.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String passwordHash;

    @Builder.Default
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback executed before the entity is persisted to the
     * database for the first time. Initializes the creation and update
     * timestamps to the current system time.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Automatically invoked by Hibernate during:
     * userRepository.save(newUser);
     * }</pre>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before an existing entity is updated in
     * the database. Refreshes the update timestamp to the current system time.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Automatically invoked by Hibernate during an update:
     * existingUser.setName("New Name");
     * userRepository.save(existingUser);
     * }</pre>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
