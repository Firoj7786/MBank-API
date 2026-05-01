package com.mbank.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mbank.dto.OtpRequest;
import com.mbank.dto.OtpVerificationRequest;
import com.mbank.dto.ResetPasswordRequest;
import com.mbank.entity.PasswordResetToken;
import com.mbank.entity.User;
import com.mbank.repository.PasswordResetTokenRepository;
import com.mbank.util.ApiMessages;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Password-reset flow.
 *
 * Security improvements over the original:
 *
 * 1. Reset tokens are stored as SHA-256 hashes, never as plaintext.
 *    A database breach therefore does not expose usable tokens.
 *
 * 2. Token lifetime is reduced from 24 hours to 15 minutes, consistent with
 *    industry practice for one-time-use reset links.
 *
 * 3. Tokens are deleted immediately after first use (one-time-use).
 *
 * 4. When the user requests a new token while a valid one already exists,
 *    the old token is invalidated first (prevents token accumulation).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** Short lifetime — reset tokens should be time-pressured. */
    private static final int EXPIRATION_MINUTES = 15;

    private final OtpService otpService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generates a cryptographically random reset token, stores its SHA-256 hash,
     * and returns the raw token to the caller (who will send it to the user by email/OTP).
     *
     * If a non-expired token already exists for this user it is revoked first so
     * only one active token per user is ever present in the database.
     */
    @Override
    @Transactional
    public String generatePasswordResetToken(User user) {
        // Revoke any pre-existing token for this user (no parallel reset sessions).
        passwordResetTokenRepository.findByUser(user)
                .ifPresent(existing -> passwordResetTokenRepository.delete(existing));

        val rawToken   = UUID.randomUUID().toString();
        val hashedToken = hashToken(rawToken);
        val expiry      = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        val resetToken = new PasswordResetToken(hashedToken, user, expiry);
        passwordResetTokenRepository.save(resetToken);

        // Return the RAW token — this is what the user receives.
        // Only the hash ever touches the database.
        return rawToken;
    }

    /**
     * Verifies a reset token supplied by the user.
     * Looks up by the hash of the supplied value, checks ownership and expiry,
     * then deletes the record (one-time use).
     */
    @Override
    @Transactional
    public boolean verifyPasswordResetToken(String rawToken, User user) {
        val hashedToken = hashToken(rawToken);

        return passwordResetTokenRepository.findByToken(hashedToken)
                .map(resetToken -> {
                    // Always delete — whether valid or not — to prevent brute-force replay.
                    passwordResetTokenRepository.delete(resetToken);

                    boolean ownerMatch  = user.equals(resetToken.getUser());
                    boolean notExpired  = resetToken.isTokenValid();
                    return ownerMatch && notExpired;
                })
                .orElse(false);
    }

    /**
     * @deprecated Kept for interface compatibility.
     * Internal deletion is now handled inside verifyPasswordResetToken.
     */
    @Override
    @Transactional
    public void deletePasswordResetToken(String rawToken) {
        val hashedToken = hashToken(rawToken);
        passwordResetTokenRepository.findByToken(hashedToken)
                .ifPresent(passwordResetTokenRepository::delete);
    }

    @Override
    public ResponseEntity<String> sendOtpForPasswordReset(OtpRequest otpRequest) {
        log.info("Received OTP request for identifier: {}", otpRequest.identifier());
        val user          = userService.getUserByIdentifier(otpRequest.identifier());
        val accountNumber = user.getAccount().getAccountNumber();
        val generatedOtp  = otpService.generateOTP(accountNumber);

        return sendOtpEmail(user, accountNumber, generatedOtp);
    }

    @Override
    public ResponseEntity<String> verifyOtpAndIssueResetToken(OtpVerificationRequest request) {
        validateOtpRequest(request);
        val user          = userService.getUserByIdentifier(request.identifier());
        val accountNumber = user.getAccount().getAccountNumber();

        if (!otpService.validateOTP(accountNumber, request.otp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiMessages.OTP_INVALID_ERROR.getMessage());
        }

        // generatePasswordResetToken returns the raw token; only the hash is stored.
        String rawResetToken = generatePasswordResetToken(user);
        return ResponseEntity.ok(
                String.format(ApiMessages.PASSWORD_RESET_TOKEN_ISSUED.getMessage(), rawResetToken));
    }

    @Override
    @Transactional 
    public ResponseEntity<String> resetPassword(ResetPasswordRequest request) {
        val user = userService.getUserByIdentifier(request.identifier());

        // verifyPasswordResetToken hashes the supplied token internally before DB lookup.
        if (!verifyPasswordResetToken(request.resetToken(), user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiMessages.TOKEN_INVALID_ERROR.getMessage());
        }

        try {
            boolean success = userService.resetPassword(user, request.newPassword());
            if (success) {
                return ResponseEntity.ok(ApiMessages.PASSWORD_RESET_SUCCESS.getMessage());
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiMessages.PASSWORD_RESET_FAILURE.getMessage());
            }
        } catch (Exception e) {
            log.error("Error resetting password for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiMessages.PASSWORD_RESET_FAILURE.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * SHA-256 hash of the raw token string.
     * Used so the database never holds a value that can be submitted directly.
     */
    private String hashToken(String rawToken) {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            val hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JVM spec — this cannot happen.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private ResponseEntity<String> sendOtpEmail(User user, String accountNumber, String generatedOtp) {
        val emailSendingFuture = otpService.sendOTPByEmail(
                user.getEmail(), user.getName(), accountNumber, generatedOtp);

        val successResponse = ResponseEntity.ok(
                String.format(ApiMessages.OTP_SENT_SUCCESS.getMessage(), user.getEmail()));
        val failureResponse = ResponseEntity.internalServerError()
                .body(String.format(ApiMessages.OTP_SENT_FAILURE.getMessage(), user.getEmail()));

        return emailSendingFuture
                .thenApply(result -> successResponse)
                .exceptionally(e -> failureResponse)
                .join();
    }

    private void validateOtpRequest(OtpVerificationRequest request) {
        if (request.identifier() == null || request.identifier().isEmpty()) {
            throw new IllegalArgumentException(ApiMessages.IDENTIFIER_MISSING_ERROR.getMessage());
        }
        if (request.otp() == null || request.otp().isEmpty()) {
            throw new IllegalArgumentException(ApiMessages.OTP_MISSING_ERROR.getMessage());
        }
    }
}