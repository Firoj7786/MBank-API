package com.mbank.repository;

import com.mbank.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);
}