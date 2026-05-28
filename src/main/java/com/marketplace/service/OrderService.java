package com.marketplace.service;

import com.marketplace.dto.PaymentRequest;
import com.marketplace.dto.PaymentResponse;
import com.marketplace.model.*;
import com.marketplace.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Create order with integrated payment processing
     */
    public Order createOrderFromCart(User user,
                                     String shippingAddress,
                                     String paymentPlan,
                                     Order.PaymentMethod paymentMethod,
                                     String paypalEmail,
                                     String cardNumber) {
        Cart cart = cartService.findByUser(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentPlan(paymentPlan);
        order.setStatus(Order.Status.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItems.add(orderItem);
            total = total.add(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);
        
        // Save order first with pending status
        Order savedOrder = orderRepository.save(order);

        // Process payment through Stripe
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setAmount(total);
        paymentRequest.setCurrency("usd");
        paymentRequest.setPaymentMethod(paymentMethod);
        paymentRequest.setDescription("Pago para el pedido #" + savedOrder.getId());
        paymentRequest.setCardToken(cardNumber);
        paymentRequest.setPaypalEmail(paypalEmail);

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        if (paymentResponse.isSuccess()) {
            // Update order with payment details
            savedOrder.setStripePaymentIntentId(paymentResponse.getPaymentIntentId());
            savedOrder.setStripeTransactionId(paymentResponse.getTransactionId());
            savedOrder.setPaymentStatus(paymentResponse.getPaymentStatus());
            applyPaymentDetails(savedOrder, paymentMethod, paypalEmail, cardNumber);
            savedOrder.setPaymentReference(paymentResponse.getPaymentIntentId());
        } else {
            savedOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
            savedOrder.setPaymentReference("FAILED-" + UUID.randomUUID());
            throw new RuntimeException("Error en el pago: " + paymentResponse.getMessage());
        }

        Order finalOrder = orderRepository.save(savedOrder);
        cartService.clearCart(user);

        return finalOrder;
    }

    /**
     * Confirm a payment after client-side verification
     */
    public Order confirmPaymentAndOrder(Long orderId, String paymentIntentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));

        PaymentResponse paymentResponse = paymentService.confirmPayment(paymentIntentId);

        if (paymentResponse.isSuccess()) {
            order.setPaymentStatus(Order.PaymentStatus.VERIFIED);
            order.setStatus(Order.Status.CONFIRMED);
            order.setStripeTransactionId(paymentResponse.getTransactionId());
        } else {
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            throw new RuntimeException("Fallo la confirmación del pago: " + paymentResponse.getMessage());
        }

        return orderRepository.save(order);
    }

    private void applyPaymentDetails(Order order,
                                     Order.PaymentMethod paymentMethod,
                                     String paypalEmail,
                                     String cardNumber) {
        if (paymentMethod == null) {
            throw new RuntimeException("Selecciona un método de pago válido.");
        }

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        if (paymentMethod == Order.PaymentMethod.PAYPAL) {
            String email = paypalEmail == null ? "" : paypalEmail.trim();
            if (email.isEmpty()) {
                throw new RuntimeException("Ingresa tu correo de PayPal.");
            }
            if (!isValidPaypalEmail(email)) {
                throw new RuntimeException("El correo de PayPal no es válido.");
            }
            order.setPaypalEmail(email);
            order.setCardLast4(null);
            return;
        }

        String normalizedCard = normalizeCardNumber(cardNumber);
        if (normalizedCard.length() < 12 || normalizedCard.length() > 19) {
            throw new RuntimeException("El número de tarjeta no es válido.");
        }
        order.setCardLast4(normalizedCard.substring(normalizedCard.length() - 4));
        order.setPaypalEmail(null);
    }

    private String normalizeCardNumber(String cardNumber) {
        String raw = cardNumber == null ? "" : cardNumber.replaceAll("[\\s-]", "");
        if (raw.isEmpty()) {
            throw new RuntimeException("Ingresa el número de tarjeta.");
        }
        if (!raw.matches("\\d+")) {
            throw new RuntimeException("El número de tarjeta debe contener solo dígitos.");
        }
        return raw;
    }

    private boolean isValidPaypalEmail(String email) {
        if (email.contains(" ")) {
            return false;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')) {
            return false;
        }
        int dotIndex = email.indexOf('.', atIndex + 2);
        return dotIndex > atIndex + 1 && dotIndex < email.length() - 1;
    }

    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findBySeller(Seller seller) {
        return orderRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    public Order updateStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public boolean sellerOwnsOrder(Seller seller, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
        
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().getSeller().getId().equals(seller.getId())) {
                return true;
            }
        }
        return false;
    }

    public Order updateStatusBySeller(Seller seller, Long orderId, Order.Status status) {
        if (!sellerOwnsOrder(seller, orderId)) {
            throw new RuntimeException("No tienes permiso para actualizar este pedido");
        }
        return updateStatus(orderId, status);
    }
}
