package com.cooksync_server.repositories;

import com.cooksync_server.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA Repository interface for Tag entity management and uniqueness checks.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    /**
     * Finds a tag by case-insensitive name matching.
     *
     * @param name tag name string
     * @return optional containing Tag if found
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * Checks if a tag with the specified case-insensitive name exists.
     *
     * @param name tag name string
     * @return true if matching tag exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Retrieves a paginated list of all tags.
     *
     * @param pageable pagination and sorting information
     * @return page of tags
     */
    Page<Tag> findAll(Pageable pageable);
}