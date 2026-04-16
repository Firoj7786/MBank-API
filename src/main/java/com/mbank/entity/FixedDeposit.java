package com.mbank.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "fixed_deposit")
public class FixedDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fdNumber;
    private double amount;
    private double interestRate;
    private int tenureMonths;
    private double maturityAmount;

    private Date startDate;
    private Date maturityDate;

    @Enumerated(EnumType.STRING)
    private FDStatus status;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore 
    private Account account;

    // Getters & Setters
}