package com.mbank.dto;

public record FundTransferRequest(String sourceAccountNumber, String targetAccountNumber, double amount, String pin) {
}
