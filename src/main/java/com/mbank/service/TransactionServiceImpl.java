package com.mbank.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mbank.dto.TransactionDTO;
import com.mbank.entity.Account;
import com.mbank.mapper.TransactionMapper;
import com.mbank.repository.AccountRepository;
import com.mbank.repository.TransactionRepository;
import com.mbank.util.pdfGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final EmailService emailService;
    private final AccountRepository accountRepository;

    @Override
    public List<TransactionDTO> getAllTransactionsByAccountNumber(String accountNumber) {
        val transactions = transactionRepository
                .findBySourceAccount_AccountNumberOrTargetAccount_AccountNumber(accountNumber, accountNumber);

        val transactionDTOs = transactions.parallelStream()
                .map(transactionMapper::toDto)
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .collect(Collectors.toList());

        return transactionDTOs;
    }
@Override
public void sendBankStatementByEmail(String accountNumber) {

    try {
        // 1. Get transactions
        List<TransactionDTO> transactions =
                getAllTransactionsByAccountNumber(accountNumber);

        // 2. Get account + email
        Account account = accountRepository.findByAccountNumber(accountNumber);
        String email = account.getUser().getEmail();

        // ⚠️ DO NOT use real password
        String pdfPassword = accountNumber.substring(accountNumber.length() - 4) + "@123";

        // 3. Generate PDF
        byte[] pdfData = pdfGenerator.generateStatementPdf(
                transactions,
                accountNumber,
                pdfPassword
        );

        // 4. Build HTML email
        String htmlContent = emailService.buildProfessionalEmail(accountNumber);

        // 5. Send email with attachment
        emailService.sendEmailWithAttachment(
                email,
                "Your M Bank Statement",
                htmlContent,
                pdfData
        );

    } catch (Exception e) {
        throw new RuntimeException("Error sending statement", e);
    }
}

}
