package com.mbank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbank.dto.OtpRequest;
import com.mbank.dto.OtpVerificationRequest;
import com.mbank.dto.ResetPasswordRequest;
import com.mbank.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/password-reset/send-otp")
    public ResponseEntity<String> sendOtpForPasswordReset(@RequestBody OtpRequest otpRequest) {
        return authService.sendOtpForPasswordReset(otpRequest);
    }

    @PostMapping("/password-reset/verify-otp")
    public ResponseEntity<String> verifyOtpAndIssueResetToken(
            @RequestBody OtpVerificationRequest otpVerificationRequest) {
        return authService.verifyOtpAndIssueResetToken(otpVerificationRequest);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        return authService.resetPassword(resetPasswordRequest);
    }

}
