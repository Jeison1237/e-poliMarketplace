package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.service.OrderService;
import com.stripe.net.Webhook;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@Slf4j
public class StripeWebhookController {

    @Autowired
    private OrderService orderService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Handle Stripe webhook events
     * POST /api/webhooks/stripe
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            // Verify the event came from Stripe
            Event event = Webhook.constructEvent(
                    payload, sigHeader, webhookSecret);

            // Handle the event
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                default:
                    log.debug("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook error: " + e.getMessage());
        }
    }

    /**
     * Handle successful payment intent
     */
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (paymentIntent == null) {
                log.error("Could not deserialize payment intent");
                return;
            }

            // Get orderId from metadata
            String orderId = paymentIntent.getMetadata().get("orderId");
            if (orderId != null) {
                Long id = Long.parseLong(orderId);
                Order order = orderService.findById(id)
                        .orElse(null);

                if (order != null) {
                    order.setPaymentStatus(Order.PaymentStatus.VERIFIED);
                    order.setStatus(Order.Status.CONFIRMED);
                    order.setStripeTransactionId(paymentIntent.getId());
                    orderService.updateStatus(id, Order.Status.CONFIRMED);
                    log.info("Payment succeeded for order: {}", id);
                }
            }
        } catch (Exception e) {
            log.error("Error processing payment_intent.succeeded event: {}", e.getMessage());
        }
    }

    /**
     * Handle failed payment intent
     */
    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (paymentIntent == null) {
                log.error("Could not deserialize payment intent");
                return;
            }

            // Get orderId from metadata
            String orderId = paymentIntent.getMetadata().get("orderId");
            if (orderId != null) {
                Long id = Long.parseLong(orderId);
                Order order = orderService.findById(id)
                        .orElse(null);

                if (order != null) {
                    order.setPaymentStatus(Order.PaymentStatus.FAILED);
                    log.warn("Payment failed for order: {}", id);
                }
            }
        } catch (Exception e) {
            log.error("Error processing payment_intent.payment_failed event: {}", e.getMessage());
        }
    }

    /**
     * Handle refunded charge
     */
    private void handleChargeRefunded(Event event) {
        try {
            log.info("Charge refunded event received");
            // In a real application, you might want to update order status to REFUNDED
        } catch (Exception e) {
            log.error("Error processing charge.refunded event: {}", e.getMessage());
        }
    }
}
