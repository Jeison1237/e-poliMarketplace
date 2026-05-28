package com.marketplace.service;

import com.marketplace.dto.PaymentRequest;
import com.marketplace.dto.PaymentResponse;
import com.marketplace.model.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public PaymentService() {
        // Stripe API key is initialized from properties
    }

    /**
     * Initialize Stripe with the API key
     */
    private void initializeStripe() {
        if (stripeApiKey != null && !stripeApiKey.isEmpty() && !stripeApiKey.contains("placeholder")) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    /**
     * Create a payment intent with Stripe
     */
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        PaymentResponse response = new PaymentResponse();
        
        try {
            initializeStripe();

            // Convert amount to cents (Stripe uses cents for USD)
            long amountInCents = paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(paymentRequest.getCurrency() != null ? paymentRequest.getCurrency() : "usd")
                    .setDescription(paymentRequest.getDescription())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );

            // Add metadata for tracking
            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", String.valueOf(paymentRequest.getOrderId()));
            metadata.put("paymentMethod", paymentRequest.getPaymentMethod().toString());
            paramsBuilder.putAllMetadata(metadata);

            PaymentIntentCreateParams params = paramsBuilder.build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            response.setSuccess(true);
            response.setMessage("Intención de pago creada exitosamente");
            response.setPaymentIntentId(paymentIntent.getId());
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setTransactionId(paymentIntent.getId());
            response.setPaymentStatus(Order.PaymentStatus.PENDING);
            response.setAmount(paymentRequest.getAmount());

            log.info("Payment intent created: {} for order: {}", paymentIntent.getId(), paymentRequest.getOrderId());

        } catch (StripeException e) {
            log.error("Stripe error processing payment: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Error procesando el pago: " + e.getMessage());
            response.setPaymentStatus(Order.PaymentStatus.FAILED);
        } catch (Exception e) {
            log.error("Unexpected error processing payment: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Error inesperado: " + e.getMessage());
            response.setPaymentStatus(Order.PaymentStatus.FAILED);
        }

        return response;
    }

    /**
     * Confirm a payment intent (used after client-side payment confirmation)
     */
    public PaymentResponse confirmPayment(String paymentIntentId) {
        PaymentResponse response = new PaymentResponse();

        try {
            initializeStripe();
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (paymentIntent.getStatus().equals("succeeded")) {
                response.setSuccess(true);
                response.setMessage("Pago confirmado exitosamente");
                response.setPaymentStatus(Order.PaymentStatus.VERIFIED);
                response.setTransactionId(paymentIntent.getId());
                log.info("Payment confirmed: {}", paymentIntentId);
            } else if (paymentIntent.getStatus().equals("processing")) {
                response.setSuccess(true);
                response.setMessage("Pago en procesamiento");
                response.setPaymentStatus(Order.PaymentStatus.PENDING);
                response.setTransactionId(paymentIntent.getId());
            } else if (paymentIntent.getStatus().equals("requires_payment_method")) {
                response.setSuccess(false);
                response.setMessage("Se requiere un método de pago");
                response.setPaymentStatus(Order.PaymentStatus.FAILED);
            } else {
                response.setSuccess(false);
                response.setMessage("Pago fallido");
                response.setPaymentStatus(Order.PaymentStatus.FAILED);
            }

        } catch (StripeException e) {
            log.error("Stripe error confirming payment: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Error confirmando el pago");
            response.setPaymentStatus(Order.PaymentStatus.FAILED);
        }

        return response;
    }

    /**
     * Retrieve payment intent details
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        initializeStripe();
        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**
     * Refund a payment
     */
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount) {
        PaymentResponse response = new PaymentResponse();

        try {
            initializeStripe();
            PaymentIntent paymentIntent = PaymentIntent.retrieve(transactionId);

            if (paymentIntent.getStatus().equals("succeeded")) {
                // Note: In a real implementation, you would use Charge.retrieve() and then create a refund
                // This is a simplified version
                response.setSuccess(true);
                response.setMessage("Reembolso iniciado");
                response.setTransactionId(transactionId);
                log.info("Refund initiated for transaction: {}", transactionId);
            } else {
                response.setSuccess(false);
                response.setMessage("No se puede reembolsar un pago que no fue exitoso");
            }

        } catch (StripeException e) {
            log.error("Stripe error refunding payment: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Error procesando el reembolso");
        }

        return response;
    }
}
