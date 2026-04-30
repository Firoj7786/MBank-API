package com.mbank.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Date transactionDate;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    @Column(unique = true)
    private String referenceId;

    private String status;

    private String description;
}