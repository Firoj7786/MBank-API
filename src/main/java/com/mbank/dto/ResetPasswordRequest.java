package com.mbank.dto;

public record ResetPasswordRequest(String identifier, String resetToken, String newPassword) {
}
