package com.marketplace.controller;

import com.marketplace.dto.PaymentResponse;
import com.marketplace.model.Order;
import com.marketplace.service.OrderService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String orderHistory(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user -> {
            model.addAttribute("orders", orderService.findByUser(user));
            model.addAttribute("user", user);
        });
        return "order-history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Authentication authentication, Model model) {
        orderService.findById(id).ifPresent(order -> model.addAttribute("order", order));
        return "order-detail";
    }

    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<PaymentResponse> checkout(@RequestParam String shippingAddress,
                           @RequestParam(defaultValue = "Pago único") String paymentPlan,
                           @RequestParam Order.PaymentMethod paymentMethod,
                           @RequestParam(required = false) String paypalEmail,
                           @RequestParam(required = false) String cardNumber,
                           Authentication authentication) {
        if (authentication == null) {
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("No autenticado");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            final PaymentResponse[] responseArray = {null};
            userService.findByUsername(authentication.getName()).ifPresent(user -> {
                Order order = orderService.createOrderFromCart(user, shippingAddress, paymentPlan,
                        paymentMethod, paypalEmail, cardNumber);
                PaymentResponse response = new PaymentResponse();
                response.setSuccess(true);
                response.setMessage("Pedido creado. Proceda con el pago.");
                response.setOrderId(order.getId());
                response.setPaymentIntentId(order.getStripePaymentIntentId());
                response.setClientSecret(order.getStripeClientSecret());
                response.setAmount(order.getTotalAmount());
                response.setPaymentStatus(order.getPaymentStatus());
                responseArray[0] = response;
            });
            
            if (responseArray[0] == null) {
                PaymentResponse response = new PaymentResponse();
                response.setSuccess(false);
                response.setMessage("No se encontró el usuario");
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(responseArray[0]);
        } catch (RuntimeException e) {
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint to confirm payment after client-side Stripe verification
     */
    @PostMapping("/{id}/confirm-payment")
    @ResponseBody
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long id,
                                                          @RequestParam String paymentIntentId) {
        try {
            Order order = orderService.confirmPaymentAndOrder(id, paymentIntentId);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setMessage("Pago confirmado exitosamente");
            response.setPaymentStatus(order.getPaymentStatus());
            response.setTransactionId(order.getStripeTransactionId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint to get payment intent details for an order
     */
    @GetMapping("/{id}/payment-status")
    @ResponseBody
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id) {
        try {
            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(order.getPaymentStatus() == Order.PaymentStatus.VERIFIED);
            response.setPaymentStatus(order.getPaymentStatus());
            response.setTransactionId(order.getStripeTransactionId());
            response.setPaymentIntentId(order.getStripePaymentIntentId());
            response.setMessage("Status: " + order.getPaymentStatus());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
