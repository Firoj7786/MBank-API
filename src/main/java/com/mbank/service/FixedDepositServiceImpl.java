package com.mbank.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbank.dto.FixedDepositResponse;
import com.mbank.entity.*;
import com.mbank.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FixedDepositServiceImpl implements FixedDepositService {

    private final FixedDepositRepository fixedDepositRepository;
    private final AccountRepository accountRepository;

    // ✅ Create FD
    @Override
    @Transactional
    public void createFD(String accountNumber, double amount, int tenureMonths, double interestRate) {

        Account account = accountRepository.findByAccountNumber(accountNumber);

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct balance
        account.setBalance(account.getBalance() - amount);

        Date startDate = new Date();

        // Simple interest
        double maturityAmount = amount + (amount * interestRate * tenureMonths / (100 * 12));

        Date maturityDate = new Date(startDate.getTime() + (long) tenureMonths * 30 * 24 * 60 * 60 * 1000);

        FixedDeposit fd = FixedDeposit.builder()
                .fdNumber("FD-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(amount)
                .interestRate(interestRate)
                .tenureMonths(tenureMonths)
                .maturityAmount(maturityAmount)
                .startDate(startDate)
                .maturityDate(maturityDate)
                .status(FDStatus.ACTIVE)
                .account(account)
                .build();

        fixedDepositRepository.save(fd);
    }

    // ✅ Get all FD
    @Override
    public List<FixedDepositResponse> getAllFDs(String accountNumber) {

        return fixedDepositRepository.findByAccount_AccountNumber(accountNumber) // ✅ PASS STRING
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ Get single FD
    @Override
    public FixedDepositResponse getFD(String fdNumber) {

        FixedDeposit fd = fixedDepositRepository.findByFdNumber(fdNumber)
                .orElseThrow(() -> new RuntimeException("FD not found"));

        return mapToResponse(fd);
    }

    // ✅ Close FD
    @Override
    @Transactional
    public void closeFD(String fdNumber) {

        FixedDeposit fd = fixedDepositRepository.findByFdNumber(fdNumber)
                .orElseThrow(() -> new RuntimeException("FD not found"));

        if (fd.getStatus() == FDStatus.CLOSED) {
            throw new RuntimeException("FD already closed");
        }

        Account account = fd.getAccount();

        // Add money back
        account.setBalance(account.getBalance() + fd.getMaturityAmount());

        fd.setStatus(FDStatus.CLOSED);

        fixedDepositRepository.save(fd);
    }

    // 🔁 Mapper
    private FixedDepositResponse mapToResponse(FixedDeposit fd) {
        return new FixedDepositResponse(
                fd.getFdNumber(),
                fd.getAmount(),
                fd.getInterestRate(),
                fd.getTenureMonths(),
                fd.getMaturityAmount(),
                fd.getStartDate(),
                fd.getMaturityDate(),
                fd.getStatus().name()
        );
    }
}