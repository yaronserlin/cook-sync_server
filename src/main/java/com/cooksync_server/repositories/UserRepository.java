package com.cooksync_server.repositories;

import com.cooksync_server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // למציאת משתמש לפי אימייל לצורך התחברות (Login)
    Optional<User> findByEmail(String email);
    
    // לבדיקה האם אימייל כבר קיים במערכת בזמן הרשמה
    boolean existsByEmail(String email);
}