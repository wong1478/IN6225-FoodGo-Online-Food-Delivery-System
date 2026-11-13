package com.in6225.fooddelivery.controller;

import com.in6225.fooddelivery.entity.Restaurant;
import com.in6225.fooddelivery.model.SessionUser;
import com.in6225.fooddelivery.service.OrderService;
import com.in6225.fooddelivery.service.RestaurantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Restaurant owner pages: restaurant profile, menu and incoming orders. */
@Controller
@RequestMapping("/owner")
public class OwnerController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public OwnerController(RestaurantService restaurantService, OrderService orderService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    // ---------- Restaurant + menu ----------

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Optional<Restaurant> restaurant = restaurantService.findByOwner(ownerId(session));
        model.addAttribute("restaurant", restaurant.orElse(null));
        model.addAttribute("menuItems", restaurant.map(r -> restaurantService.getFullMenu(r.getId())).orElse(List.of()));
        return "owner/dashboard";
    }

    @PostMapping("/restaurant")
    public String saveRestaurant(@RequestParam String name, @RequestParam(required = false) String cuisine,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(defaultValue = "false") boolean open,
                                 HttpSession session, RedirectAttributes redirect) {
        try {
            restaurantService.saveOwnerRestaurant(ownerId(session), name, cuisine, address, description, open);
            redirect.addFlashAttribute("success", "Restaurant details saved.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner";
    }

    @PostMapping("/menu")
    public String addMenuItem(@RequestParam String name, @RequestParam(required = false) String description,
                              @RequestParam BigDecimal price, HttpSession session, RedirectAttributes redirect) {
        try {
            restaurantService.addMenuItem(ownerId(session), name, description, price);
            redirect.addFlashAttribute("success", name + " added to the menu.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner";
    }

    @GetMapping("/menu/{id}/edit")
    public String editMenuItemPage(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("item", restaurantService.findOwnedMenuItem(ownerId(session), id));
        return "owner/menu-edit";
    }

    @PostMapping("/menu/{id}")
    public String updateMenuItem(@PathVariable Long id, @RequestParam String name,
                                 @RequestParam(required = false) String description, @RequestParam BigDecimal price,
                                 @RequestParam(defaultValue = "false") boolean available,
                                 HttpSession session, RedirectAttributes redirect) {
        try {
            restaurantService.updateMenuItem(ownerId(session), id, name, description, price, available);
            redirect.addFlashAttribute("success", "Menu item updated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner";
    }

    @PostMapping("/menu/{id}/toggle")
    public String toggleMenuItem(@PathVariable Long id, HttpSession session) {
        restaurantService.toggleAvailability(ownerId(session), id);
        return "redirect:/owner";
    }

    // ---------- Orders ----------

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        Optional<Restaurant> restaurant = restaurantService.findByOwner(ownerId(session));
        model.addAttribute("restaurant", restaurant.orElse(null));
        model.addAttribute("orders", restaurant.map(r -> orderService.findByRestaurant(r.getId())).orElse(List.of()));
        return "owner/orders";
    }

    @PostMapping("/orders/{id}/{action}")
    public String updateOrder(@PathVariable Long id, @PathVariable String action,
                              HttpSession session, RedirectAttributes redirect) {
        Long ownerId = ownerId(session);
        try {
            switch (action) {
                case "accept" -> orderService.acceptOrder(id, ownerId);
                case "reject" -> orderService.rejectOrder(id, ownerId);
                case "ready" -> orderService.markReady(id, ownerId);
                case "complete" -> orderService.markCompleted(id, ownerId);
                default -> throw new IllegalArgumentException("Unknown action.");
            }
            redirect.addFlashAttribute("success", "Order #" + id + " updated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/orders";
    }

    private Long ownerId(HttpSession session) {
        return ((SessionUser) session.getAttribute(SessionUser.KEY)).getId();
    }
}
