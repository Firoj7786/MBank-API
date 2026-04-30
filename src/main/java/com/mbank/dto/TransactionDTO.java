package com.mbank.dto;

import java.util.Date;

import com.mbank.entity.Transaction;
import com.mbank.entity.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private double amount;
    private TransactionType transactionType;
    private Date transactionDate;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private String referenceId;
    private String status;
    private String description;

    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.transactionType = transaction.getTransactionType();
        this.transactionDate = transaction.getTransactionDate();
        this.referenceId = transaction.getReferenceId();
        this.status = transaction.getStatus();
        this.description = transaction.getDescription();

        // ✅ FIX: null check BEFORE calling getAccountNumber() — was crashing for
        //         gateway/webhook credits where sourceAccount is intentionally null
        this.sourceAccountNumber = transaction.getSourceAccount() != null
                ? transaction.getSourceAccount().getAccountNumber()
                : "N/A";

        this.targetAccountNumber = transaction.getTargetAccount() != null
                ? transaction.getTargetAccount().getAccountNumber()
                : "N/A";
    }
}