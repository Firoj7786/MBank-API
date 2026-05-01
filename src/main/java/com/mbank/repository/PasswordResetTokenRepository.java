package com.mbank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mbank.entity.PasswordResetToken;
import com.mbank.entity.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Look up by the SHA-256 hash of the raw token.
     * Returns Optional so callers handle the "not found" case explicitly.
     */
    Optional<PasswordResetToken> findByToken(String hashedToken);

    /**
     * Returns any active token record for this user.
     * Changed return type to Optional to avoid null-check anti-pattern.
     */
    Optional<PasswordResetToken> findByUser(User user);
}