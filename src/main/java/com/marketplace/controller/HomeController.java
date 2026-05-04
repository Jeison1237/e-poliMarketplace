package com.marketplace.controller;

import com.marketplace.service.ProductService;
import com.marketplace.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerService sellerService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productService.findFeatured());
        model.addAttribute("recentProducts", productService.findAll());
        model.addAttribute("popularSellers", sellerService.findAllActive());
        model.addAttribute("categories", productService.findAllCategories());
        return "index";
    }
}
