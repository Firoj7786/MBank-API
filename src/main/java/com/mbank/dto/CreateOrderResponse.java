package com.mbank.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderResponse {
    private String orderId;
    private Double amount;
    private String currency;
    private String key;
}
