package com.cooksync_server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cooksync_server.entities.PasswordResetToken;

/**
 * Spring Data JPA Repository interface for PasswordResetToken entity management.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 05/08/2026
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /**
     * Finds a PasswordResetToken entity by its unique token string.
     *
     * @param token reset token string value
     * @return optional containing PasswordResetToken if located
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Deletes all outstanding reset tokens for a specific user ID, so a fresh forgot-password
     * request invalidates any earlier unused token for the same account.
     *
     * @param userId unique user identifier
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
