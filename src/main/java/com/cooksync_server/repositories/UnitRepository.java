package com.cooksync_server.repositories;

import com.cooksync_server.entities.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for Unit entity operations.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, String> {

    /**
     * Finds a measurement unit entity by its unique symbol code.
     *
     * @param code unit symbol code (e.g. "kg", "tsp")
     * @return optional containing Unit if found
     */
    Optional<Unit> findByCode(String code);

    /**
     * Retrieves all measurement units with pagination.
     *
     * @param pageable pagination parameters
     * @return page of Unit entities
     */
    org.springframework.data.domain.Page<Unit> findAll(org.springframework.data.domain.Pageable pageable);
}