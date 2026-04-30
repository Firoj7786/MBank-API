package com.mbank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;

    private Long userId;

    private Double amount;

    private String currency;

    private String status; // CREATED, PAID, FAILED

    private String receipt;

    private LocalDateTime createdAt;
}
