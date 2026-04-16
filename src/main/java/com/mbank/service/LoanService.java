package com.mbank.service;

import java.util.List;

import com.mbank.dto.LoanRequest;
import com.mbank.dto.LoanResponse;

public interface LoanService {

    LoanResponse applyLoan(String accountNumber, LoanRequest request);

    List<LoanResponse> getAllLoans(String accountNumber);

    LoanResponse getLoan(String loanNumber);
}