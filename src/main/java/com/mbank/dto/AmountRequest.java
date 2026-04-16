package com.mbank.dto;

public record AmountRequest(String accountNumber, String pin, double amount) {
}
