package com.mbank.service;

import org.springframework.http.ResponseEntity;

import com.mbank.dto.OtpRequest;
import com.mbank.dto.OtpVerificationRequest;
import com.mbank.dto.ResetPasswordRequest;
import com.mbank.entity.User;

public interface AuthService {
    public String generatePasswordResetToken(User user);

    public boolean verifyPasswordResetToken(String token, User user);

    public void deletePasswordResetToken(String token);

    public ResponseEntity<String> sendOtpForPasswordReset(OtpRequest otpRequest);

    public ResponseEntity<String> verifyOtpAndIssueResetToken(OtpVerificationRequest otpVerificationRequest);

    public ResponseEntity<String> resetPassword(ResetPasswordRequest resetPasswordRequest);

}
