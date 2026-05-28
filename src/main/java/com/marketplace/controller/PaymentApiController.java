package com.marketplace.controller;

import com.marketplace.dto.PaymentRequest;
import com.marketplace.dto.PaymentResponse;
import com.marketplace.model.Order;
import com.marketplace.service.OrderService;
import com.marketplace.service.PaymentService;
import com.marketplace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentApiController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * Create a payment intent for an order
     * POST /api/payments/create-intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest request,
                                                               Authentication authentication) {
        if (authentication == null) {
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage("No autenticado");
            return ResponseEntity.status(401).body(error);
        }

        try {
            // Validate order exists and belongs to user
            Order order = orderService.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            userService.findByUsername(authentication.getName()).ifPresentOrElse(user -> {
                if (!order.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("No tienes permiso para este pedido");
                }
            }, () -> {
                throw new RuntimeException("Usuario no encontrado");
            });

            PaymentResponse response = paymentService.processPayment(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Confirm a payment after Stripe processing
     * POST /api/payments/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestParam String paymentIntentId,
                                                          @RequestParam Long orderId,
                                                          Authentication authentication) {
        if (authentication == null) {
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage("No autenticado");
            return ResponseEntity.status(401).body(error);
        }

        try {
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            userService.findByUsername(authentication.getName()).ifPresentOrElse(user -> {
                if (!order.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("No tienes permiso para este pedido");
                }
            }, () -> {
                throw new RuntimeException("Usuario no encontrado");
            });

            Order confirmedOrder = orderService.confirmPaymentAndOrder(orderId, paymentIntentId);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setMessage("Pago confirmado exitosamente");
            response.setPaymentStatus(confirmedOrder.getPaymentStatus());
            response.setTransactionId(confirmedOrder.getStripeTransactionId());
            response.setPaymentIntentId(confirmedOrder.getStripePaymentIntentId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage());
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get payment status for an order
     * GET /api/payments/status/{orderId}
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long orderId,
                                                            Authentication authentication) {
        if (authentication == null) {
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage("No autenticado");
            return ResponseEntity.status(401).body(error);
        }

        try {
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            userService.findByUsername(authentication.getName()).ifPresentOrElse(user -> {
                if (!order.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("No tienes permiso para este pedido");
                }
            }, () -> {
                throw new RuntimeException("Usuario no encontrado");
            });

            PaymentResponse response = new PaymentResponse();
            response.setSuccess(order.getPaymentStatus() == Order.PaymentStatus.VERIFIED);
            response.setPaymentStatus(order.getPaymentStatus());
            response.setTransactionId(order.getStripeTransactionId());
            response.setPaymentIntentId(order.getStripePaymentIntentId());
            response.setAmount(order.getTotalAmount());
            response.setMessage("Estado del pago: " + order.getPaymentStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage());
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Refund a payment
     * POST /api/payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@RequestParam Long orderId,
                                                         Authentication authentication) {
        if (authentication == null) {
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage("No autenticado");
            return ResponseEntity.status(401).body(error);
        }

        try {
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            userService.findByUsername(authentication.getName()).ifPresentOrElse(user -> {
                if (!order.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("No tienes permiso para este pedido");
                }
            }, () -> {
                throw new RuntimeException("Usuario no encontrado");
            });

            if (order.getStripeTransactionId() == null) {
                throw new RuntimeException("Este pedido no tiene un pago válido para reembolsar");
            }

            PaymentResponse response = paymentService.refundPayment(order.getStripeTransactionId(), order.getTotalAmount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error refunding payment: {}", e.getMessage());
            PaymentResponse error = new PaymentResponse();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
