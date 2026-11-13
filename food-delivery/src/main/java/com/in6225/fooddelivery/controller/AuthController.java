package com.in6225.fooddelivery.controller;

import com.in6225.fooddelivery.entity.Role;
import com.in6225.fooddelivery.entity.User;
import com.in6225.fooddelivery.model.SessionUser;
import com.in6225.fooddelivery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, Model model) {
        Optional<User> user = userService.login(email, password);
        if (user.isEmpty()) {
            model.addAttribute("error", "Wrong email or password.");
            model.addAttribute("email", email);
            return "login";
        }
        session.setAttribute(SessionUser.KEY, new SessionUser(user.get()));

        // Send each role to its own home page
        return switch (user.get().getRole()) {
            case RESTAURANT_OWNER -> "redirect:/owner";
            case ADMIN -> "redirect:/admin";
            default -> "redirect:/";
        };
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                           @RequestParam(required = false) String phone, @RequestParam(required = false) String address,
                           @RequestParam Role role, Model model, RedirectAttributes redirect) {
        try {
            userService.register(name, email, password, phone, address, role);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        redirect.addFlashAttribute("success", "Account created. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
