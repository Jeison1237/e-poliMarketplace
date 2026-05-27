package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.service.OrderService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String checkout(@RequestParam String shippingAddress,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        try {
            userService.findByUsername(authentication.getName()).ifPresent(user -> {
                Order order = orderService.createOrderFromCart(user, shippingAddress);
                redirectAttributes.addFlashAttribute("success",
                        "¡Pedido #" + order.getId() + " creado exitosamente!");
            });
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
        return "redirect:/orders";
    }
}
