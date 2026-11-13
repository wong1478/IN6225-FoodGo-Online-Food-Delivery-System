package com.in6225.fooddelivery.controller;

import com.in6225.fooddelivery.service.OrderService;
import com.in6225.fooddelivery.service.RestaurantService;
import com.in6225.fooddelivery.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Platform admin: read-only overview of users, restaurants and orders. */
@Controller
public class AdminController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public AdminController(UserService userService, RestaurantService restaurantService, OrderService orderService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("restaurants", restaurantService.findAll());
        model.addAttribute("orders", orderService.findAll());
        return "admin/dashboard";
    }
}
