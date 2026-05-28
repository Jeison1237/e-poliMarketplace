package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.model.Seller;
import com.marketplace.service.ImageService;
import com.marketplace.service.ProductService;
import com.marketplace.service.SellerService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Autowired
    private ImageService imageService;

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
            String message = "Hola, quisiera información sobre tu tienda " + seller.getStoreName();
            String whatsappLink = buildWhatsappLink(seller.getUser().getPhone(), message);
            model.addAttribute("whatsappLink", whatsappLink);
        });
        return "seller-profile";
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
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
                               @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        try {
            userService.findByUsername(authentication.getName()).ifPresent(user -> {
                seller.setUser(user);
                user.setRole(com.marketplace.model.User.Role.SELLER);
                userService.update(user);
                
                // Handle logo file upload
                if (logoFile != null && !logoFile.isEmpty()) {
                    try {
                        String logoPath = imageService.saveImage(logoFile);
                        seller.setLogoUrl(logoPath);
                    } catch (IOException e) {
                        redirectAttributes.addFlashAttribute("error", "Error al subir el logo: " + e.getMessage());
                        return;
                    } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("error", "Archivo inválido: " + e.getMessage());
                        return;
                    }
                }
                
                sellerService.createSeller(seller);
            });
            redirectAttributes.addFlashAttribute("success", "¡Tu tienda ha sido creada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la tienda: " + e.getMessage());
        }
        return "redirect:/sellers/dashboard";
    }

    private String buildWhatsappLink(String phone, String message) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String normalized = phone.replaceAll("\\D", "");
        if (normalized.isBlank()) {
            return null;
        }
        if (message == null || message.isBlank()) {
            return "https://wa.me/" + normalized;
        }
        return "https://wa.me/" + normalized + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
