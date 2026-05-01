package com.mbank.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores a SHA-256 hash of the user's password reset token.
 *
 * The raw token is sent to the user (via email/OTP) and is NEVER persisted here.
 * Only the hash is stored, so a database breach does not yield usable tokens.
 *
 * Tokens expire after 15 minutes and are deleted on first use.
 */
@Entity
@NoArgsConstructor
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hex digest of the raw token string.
     * Never store the raw token here.
     */
    @NotEmpty
    @Column(unique = true, length = 64) // SHA-256 hex = 64 chars
    private String token;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // one active token per user
    private User user;

    @NotNull
    private LocalDateTime expiryDateTime;

    public PasswordResetToken(String hashedToken, User user, LocalDateTime expiryDateTime) {
        this.token        = hashedToken;
        this.user         = user;
        this.expiryDateTime = expiryDateTime;
    }

    /** Returns true if the token has not yet passed its expiry time. */
    public boolean isTokenValid() {
        return LocalDateTime.now().isBefore(expiryDateTime);
    }
}