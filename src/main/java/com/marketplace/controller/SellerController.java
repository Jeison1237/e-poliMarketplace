package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.model.Seller;
import com.marketplace.service.ProductService;
import com.marketplace.service.SellerService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/sellers")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listSellers(Model model) {
        model.addAttribute("sellers", sellerService.findAllActive());
        return "sellers";
    }

    @GetMapping("/{id}")
    public String sellerProfile(@PathVariable Long id, Model model) {
        sellerService.findById(id).ifPresent(seller -> {
            model.addAttribute("seller", seller);
            model.addAttribute("products", productService.findBySeller(seller));
        });
        return "seller-profile";
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(Authentication authentication, Model model) {
        userService.findByUsername(authentication.getName()).ifPresent(user -> {
            sellerService.findByUser(user).ifPresent(seller -> {
                model.addAttribute("seller", seller);
                model.addAttribute("products", productService.findBySeller(seller));
            });
            model.addAttribute("user", user);
        });
        return "seller-dashboard";
    }

    @GetMapping("/become-seller")
    public String becomeSellerPage(Model model) {
        model.addAttribute("seller", new Seller());
        return "become-seller";
    }

    @PostMapping("/become-seller")
    public String becomeSeller(@ModelAttribute Seller seller,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        userService.findByUsername(authentication.getName()).ifPresent(user -> {
            seller.setUser(user);
            user.setRole(com.marketplace.model.User.Role.SELLER);
            userService.update(user);
            sellerService.createSeller(seller);
        });
        redirectAttributes.addFlashAttribute("success", "¡Tu tienda ha sido creada exitosamente!");
        return "redirect:/sellers/dashboard";
    }
}
