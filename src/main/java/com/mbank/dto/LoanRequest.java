package com.mbank.dto;

public record LoanRequest(
        double amount,
        int tenureMonths
) {}