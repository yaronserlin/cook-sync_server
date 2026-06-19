package com.cooksync_server.repositories;

import com.cooksync_server.entities.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, String> {
    Optional<Unit> findByCode(String code);
}