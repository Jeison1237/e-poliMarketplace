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

import java.math.BigDecimal;
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
        });
        return "product-detail";
    }

    @GetMapping("/new")
    public String newProductPage(Authentication authentication, Model model) {
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                sellerService.findByUser(user).ifPresent(seller ->
                        model.addAttribute("seller", seller)));
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @PostMapping("/new")
    public String createProduct(@ModelAttribute Product product,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        userService.findByUsername(authentication.getName()).ifPresent(user ->
                sellerService.findByUser(user).ifPresent(seller -> {
                    product.setSeller(seller);
                    productService.save(product);
                }));
        redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente.");
        return "redirect:/sellers/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String editProductPage(@PathVariable Long id, Authentication authentication, Model model) {
        productService.findById(id).ifPresent(product -> model.addAttribute("product", product));
        return "product-form";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute Product updatedProduct,
                                RedirectAttributes redirectAttributes) {
        productService.findById(id).ifPresent(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setImageUrl(updatedProduct.getImageUrl());
            product.setCategory(updatedProduct.getCategory());
            product.setStock(updatedProduct.getStock());
            product.setFeatured(updatedProduct.isFeatured());
            productService.update(product);
        });
        redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente.");
        return "redirect:/sellers/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente.");
        return "redirect:/sellers/dashboard";
    }
}
