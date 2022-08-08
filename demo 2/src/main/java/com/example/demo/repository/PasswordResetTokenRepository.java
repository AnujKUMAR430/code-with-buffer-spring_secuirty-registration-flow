package com.example.demo.repository;

import com.example.demo.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken , Long> {
    static PasswordResetToken findByToken(String token) {
        return null;
    }
}
