package com.mbank.service;

import java.util.List;

import com.mbank.dto.FixedDepositResponse;

public interface FixedDepositService {

    void createFD(String accountNumber, double amount, int tenureMonths, double interestRate);

    List<FixedDepositResponse> getAllFDs(String accountNumber);

    FixedDepositResponse getFD(String fdNumber);

    void closeFD(String fdNumber);
}