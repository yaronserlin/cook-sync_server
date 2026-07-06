package com.cooksync_server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooksync_server.entities.User;

/**
 * Repository interface for managing User entity persistence and retrieval
 * operations. Extends JpaRepository to provide standard CRUD functionality
 * alongside custom query methods for authentication.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Retrieves a user associated with the specified email address.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * Optional<User> user = userRepository.findByEmail("alice@example.com");
     * if (user.isPresent()) {
     * // process login
     * }
     * }</pre>
     *
     * @param email The exact email address to search for in the database.
     * @return An {@link Optional} containing the {@link User} if found, or an
     * empty Optional if no matching user exists.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user record with the given email address already exists in
     * the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * boolean isTaken = userRepository.existsByEmail("alice@example.com");
     * if (isTaken) {
     * throw new UserAlreadyExistsException("Email is already registered");
     * }
     * }</pre>
     *
     * @param email The email address to verify for availability during
     * registration.
     * @return {@code true} if a user with the specified email exists,
     * {@code false} otherwise.
     */
    boolean existsByEmail(String email);
}
