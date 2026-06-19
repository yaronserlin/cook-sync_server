package com.cooksync_server.repositories;

import com.cooksync_server.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    
    // לבדיקה ומניעת כפילות תגיות לפני יצירה
    Optional<Tag> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
}