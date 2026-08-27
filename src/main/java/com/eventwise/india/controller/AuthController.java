package com.eventtoimpact.india.controller;

import com.eventtoimpact.india.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final AccountService accounts;

    public AuthController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/register")
    public String registrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        try {
            accounts.register(username, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("registrationError", exception.getMessage());
            redirectAttributes.addFlashAttribute("attemptedUsername", username == null ? "" : username.trim());
            return "redirect:/register";
        }
    }
}
