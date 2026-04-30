package com.mbank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbank.dto.CreateOrderRequest;
import com.mbank.dto.VerifyPaymentRequest;
import com.mbank.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) throws Exception {
        // ✅ Authentication param removed — not needed, userId resolved inside service
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyPaymentRequest request) {
        // ✅ FIX: Removed @RequestHeader("userId") Long userId — never trust frontend for userId
        // userId is now resolved securely inside PaymentServiceImpl via LoggedinUser
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(orderId));
    }
}