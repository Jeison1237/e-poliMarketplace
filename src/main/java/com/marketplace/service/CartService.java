package com.marketplace.service;

import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    public Cart addItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);

        if (quantity <= 0) {
            return removeItem(user, productId);
        }

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        return cartRepository.save(cart);
    }

    public Cart removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return cartRepository.save(cart);
    }

    public Cart clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    public Cart findByUser(User user) {
        return getOrCreateCart(user);
    }
}
