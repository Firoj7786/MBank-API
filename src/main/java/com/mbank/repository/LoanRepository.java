package com.mbank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mbank.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByAccount_AccountNumber(String accountNumber);

    Optional<Loan> findByLoanNumber(String loanNumber);
}