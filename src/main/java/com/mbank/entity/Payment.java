package com.mbank.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentId;

    private String orderId;

    private Long userId;

    private Double amount;

    private String method; // UPI, CARD, NETBANKING

    private String status; // SUCCESS, FAILED

    @Column(length = 1000)
    private String signature;

    private LocalDateTime createdAt;
}
