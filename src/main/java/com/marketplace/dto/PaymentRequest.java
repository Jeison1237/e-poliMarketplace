package com.marketplace.dto;

import com.marketplace.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private Order.PaymentMethod paymentMethod;
    private String cardToken;
    private String paypalEmail;
    private String description;
}
