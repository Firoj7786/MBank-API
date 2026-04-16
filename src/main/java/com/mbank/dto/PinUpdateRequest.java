package com.mbank.dto;

public record PinUpdateRequest(String accountNumber, String oldPin, String newPin, String password) {
}
