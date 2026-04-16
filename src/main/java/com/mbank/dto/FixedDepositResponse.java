package com.mbank.dto;

import java.util.Date;

import lombok.*;

@Data
@AllArgsConstructor
public class FixedDepositResponse {

    private String fdNumber;
    private double amount;
    private double interestRate;
    private int tenureMonths;
    private double maturityAmount;
    private Date startDate;
    private Date maturityDate;
    private String status;
}