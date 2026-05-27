package com.marketplace.controller;

import com.marketplace.model.Cart;
import com.marketplace.service.CartService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user -> {
            Cart cart = cartService.findByUser(user);
            model.addAttribute("cart", cart);
        });
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                cartService.addItem(user, productId, quantity));
        redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito.");
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                cartService.updateItemQuantity(user, productId, quantity));
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                cartService.removeItem(user, productId));
        redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                cartService.clearCart(user));
        return "redirect:/cart";
    }
}
