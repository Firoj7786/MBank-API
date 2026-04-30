package com.mbank.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mbank.dto.LoanRequest;
import com.mbank.dto.LoanResponse;
import com.mbank.entity.Account;
import com.mbank.entity.Loan;
import com.mbank.entity.LoanStatus;
import com.mbank.repository.AccountRepository;
import com.mbank.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;

    @Override
    public LoanResponse applyLoan(String accountNumber, LoanRequest request) {

        Account account = accountRepository.findByAccountNumber(accountNumber);

        double interestRate = 11.0; // default
        int tenure = request.tenureMonths();
        double amount = request.amount();

        // EMI Formula
        double monthlyRate = interestRate / 12 / 100;
        double emi = (amount * monthlyRate * Math.pow(1 + monthlyRate, tenure)) /
                (Math.pow(1 + monthlyRate, tenure) - 1);

        double totalPayable = emi * tenure;

        Loan loan = Loan.builder()
                .loanNumber("LN-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(amount)
                .interestRate(interestRate)
                .tenureMonths(tenure)
                .emi(emi)
                .totalPayable(totalPayable)
                .status(LoanStatus.PENDING)
                .createdAt(new Date())
                .account(account)
                .build();

        loanRepository.save(loan);

        return mapToResponse(loan);
    }

    @Override
    public List<LoanResponse> getAllLoans(String accountNumber) {

        return loanRepository.findByAccount_AccountNumber(accountNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LoanResponse getLoan(String loanNumber) {

        Loan loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        return mapToResponse(loan);
    }

    private LoanResponse mapToResponse(Loan loan) {
        return new LoanResponse(
                loan.getLoanNumber(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getEmi(),
                loan.getTotalPayable(),
                loan.getStatus().name(),
                loan.getCreatedAt()
        );
    }
}