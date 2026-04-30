package com.mbank.service;

import org.springframework.security.core.Authentication;

import com.mbank.dto.CreateOrderRequest;
import com.mbank.dto.CreateOrderResponse;
import com.mbank.dto.VerifyPaymentRequest;

public interface PaymentService {

	CreateOrderResponse createOrder(CreateOrderRequest request) throws Exception;

    String verifyPayment(VerifyPaymentRequest request);

    String getPaymentStatus(String orderId);
    
    void processWebhookPayment(String orderId, String paymentId);
}
