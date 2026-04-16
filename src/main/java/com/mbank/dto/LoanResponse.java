package com.mbank.dto;

import java.util.Date;

public record LoanResponse(
        String loanNumber,
        double amount,
        double interestRate,
        int tenureMonths,
        double emi,
        double totalPayable,
        String status,
        Date createdAt
) {}