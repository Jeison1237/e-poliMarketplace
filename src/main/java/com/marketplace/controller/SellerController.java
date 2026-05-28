package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.Seller;
import com.marketplace.service.ImageService;
import com.marketplace.service.OrderService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private OrderService orderService;

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
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user -> {
            sellerService.findByUser(user).ifPresent(seller -> {
                model.addAttribute("seller", seller);
                model.addAttribute("products", productService.findBySeller(seller));
                model.addAttribute("sellerOrders", orderService.findBySeller(seller));
            });
            model.addAttribute("user", user);
        });
        return "seller-dashboard";
    }

    @PostMapping("/orders/{id}/status")
    @ResponseBody
    public Map<String, Object> updateOrderStatus(@PathVariable Long id,
                                                   @RequestParam Order.Status status,
                                                   Authentication authentication,
                                                   RedirectAttributes redirectAttributes) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "No autenticado");
            return response;
        }
        
        try {
            userService.findByUsername(authentication.getName()).ifPresent(user -> {
                sellerService.findByUser(user).ifPresent(seller -> {
                    Order updatedOrder = orderService.updateStatusBySeller(seller, id, status);
                    response.put("success", true);
                    response.put("message", "Pedido actualizado a: " + status);
                    response.put("newStatus", updatedOrder.getStatus().name());
                });
            });
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
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
}
