package com.marketplace.dto;

import com.marketplace.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String paymentIntentId;
    private Order.PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String clientSecret;
    private String redirectUrl;
    private Long orderId;
}
