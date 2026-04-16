package com.mbank.dto;

public record LoginRequest(String identifier, String password, boolean useOtp) {
}
