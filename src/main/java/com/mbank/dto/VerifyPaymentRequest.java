package com.mbank.dto;

import lombok.Data;

@Data
public class VerifyPaymentRequest {
    private String orderId;
    private String paymentId;
    private String signature;
}
