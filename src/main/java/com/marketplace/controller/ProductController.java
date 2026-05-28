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
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @GetMapping
    public String listProducts(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) BigDecimal minPrice,
                               @RequestParam(required = false) BigDecimal maxPrice,
                               Model model) {
        List<Product> products;
        if (keyword != null || category != null || minPrice != null || maxPrice != null) {
            products = productService.search(keyword, category, minPrice, maxPrice);
        } else {
            products = productService.findAll();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "products";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        productService.findById(id).ifPresent(product -> {
            model.addAttribute("product", product);
            model.addAttribute("sellerProducts",
                    productService.findBySeller(product.getSeller()).stream()
                            .filter(p -> !p.getId().equals(id))
                            .limit(4)
                            .toList());
            String message = "Hola, me interesa el producto " + product.getName() + " de " + product.getSeller().getStoreName();
            String whatsappLink = buildWhatsappLink(product.getSeller().getUser().getPhone(), message);
            model.addAttribute("whatsappLink", whatsappLink);
        });
        return "product-detail";
    }

    @GetMapping("/new")
    public String newProductPage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                sellerService.findByUser(user).ifPresent(seller ->
                        model.addAttribute("seller", seller)));
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @PostMapping("/new")
    public String createProduct(@ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        try {
            userService.findByUsername(authentication.getName()).ifPresent(user ->
                    sellerService.findByUser(user).ifPresent(seller -> {
                        product.setSeller(seller);
                        
                        // Handle image upload
                        if (imageFile != null && !imageFile.isEmpty()) {
                            try {
                                String imagePath = imageService.saveImage(imageFile);
                                product.setImageUrl(imagePath);
                            } catch (IOException e) {
                                redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                                return;
                            } catch (IllegalArgumentException e) {
                                redirectAttributes.addFlashAttribute("error", "Archivo inválido: " + e.getMessage());
                                return;
                            }
                        }
                        
                        productService.save(product);
                    }));
            redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: " + e.getMessage());
        }
        return "redirect:/sellers/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String editProductPage(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        productService.findById(id).ifPresent(product -> model.addAttribute("product", product));
        return "product-form";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute Product updatedProduct,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.findById(id).ifPresent(product -> {
                product.setName(updatedProduct.getName());
                product.setDescription(updatedProduct.getDescription());
                product.setPrice(updatedProduct.getPrice());
                product.setCategory(updatedProduct.getCategory());
                product.setStock(updatedProduct.getStock());
                product.setFeatured(updatedProduct.isFeatured());
                
                // Handle new image upload
                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        // Delete old image if it's a local file
                        if (product.getImageUrl() != null && !product.getImageUrl().startsWith("http")) {
                            imageService.deleteImage(product.getImageUrl());
                        }
                        
                        String imagePath = imageService.saveImage(imageFile);
                        product.setImageUrl(imagePath);
                    } catch (IOException e) {
                        redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                        return;
                    } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("error", "Archivo inválido: " + e.getMessage());
                        return;
                    }
                } else if (updatedProduct.getImageUrl() != null && !updatedProduct.getImageUrl().isEmpty()) {
                    // If a new URL is provided directly
                    product.setImageUrl(updatedProduct.getImageUrl());
                }
                
                productService.update(product);
            });
            redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el producto: " + e.getMessage());
        }
        return "redirect:/sellers/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.findById(id).ifPresent(product -> {
            // Delete associated image if it's a local file
            if (product.getImageUrl() != null && !product.getImageUrl().startsWith("http")) {
                imageService.deleteImage(product.getImageUrl());
            }
        });
        
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente.");
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
