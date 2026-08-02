package com.cooksync_server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cooksync_server.entities.User;

/**
 * Spring Data JPA Repository interface for managing User entity persistence.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Retrieves a user entity matching the provided email address.
     *
     * @param email exact email address to search for
     * @return optional containing User if matching account exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifies if a user with the specified email address exists.
     *
     * @param email target email address
     * @return true if email is registered, false otherwise
     */
    boolean existsByEmail(String email);
}
