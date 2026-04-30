package com.mbank.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusResponse {
    private String orderId;
    private String status;
    private Double amount;
}
