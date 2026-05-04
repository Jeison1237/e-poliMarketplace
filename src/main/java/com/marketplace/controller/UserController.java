package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("success", "Cuenta creada exitosamente. Por favor inicia sesión.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        userService.findByUsername(authentication.getName())
                .ifPresent(user -> model.addAttribute("user", user));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User updatedUser,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        userService.findByUsername(authentication.getName()).ifPresent(user -> {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPhone(updatedUser.getPhone());
            user.setAddress(updatedUser.getAddress());
            userService.update(user);
        });
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        return "redirect:/profile";
    }
}
