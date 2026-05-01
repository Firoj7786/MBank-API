package com.mbank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbank.dto.LoginRequest;
import com.mbank.dto.OtpRequest;
import com.mbank.dto.OtpVerificationRequest;
import com.mbank.entity.User;
import com.mbank.exception.InvalidTokenException;
import com.mbank.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request)
            throws InvalidTokenException {
        return userService.login(loginRequest, request);
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<String> generateOtp(@RequestBody OtpRequest otpRequest) {
        return userService.generateOtp(otpRequest);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtpAndLogin(@RequestBody OtpVerificationRequest otpVerificationRequest)
            throws InvalidTokenException {
        return userService.verifyOtpAndLogin(otpVerificationRequest);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    /**
     * Logout via POST (GET was incorrect — logout mutates server state).
     * The Authorization header is read directly; no request body needed.
     * Returns 200 OK on success.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token)
            throws InvalidTokenException {
        return userService.logout(token);
    }
}