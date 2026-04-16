package com.mbank.dto;

import lombok.Data;

@Data
public class FDRequest {
    private double amount;
    private int tenureMonths;
    private double interestRate;
}