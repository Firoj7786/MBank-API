package com.mbank.repository;

import com.mbank.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaymentId(String paymentId); 

    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByOrderId(String orderId);
}
