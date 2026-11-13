package com.in6225.fooddelivery.controller;

import com.in6225.fooddelivery.entity.Order;
import com.in6225.fooddelivery.model.SessionUser;
import com.in6225.fooddelivery.service.OrderService;
import com.in6225.fooddelivery.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Customer's orders: history, tracking, cancel and review. */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;

    public OrderController(OrderService orderService, ReviewService reviewService) {
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String myOrders(HttpSession session, Model model) {
        model.addAttribute("orders", orderService.findByCustomer(currentUserId(session)));
        return "orders";
    }

    @GetMapping("/{id}")
    public String trackOrder(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirect) {
        Order order = orderService.findById(id);
        if (!order.getCustomer().getId().equals(currentUserId(session))) {
            redirect.addFlashAttribute("error", "That order does not belong to you.");
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("review", reviewService.findByOrder(id).orElse(null));
        return "order-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            orderService.cancelByCustomer(id, currentUserId(session));
            redirect.addFlashAttribute("success", "Order cancelled.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id, @RequestParam int rating, @RequestParam(required = false) String comment,
                         HttpSession session, RedirectAttributes redirect) {
        try {
            reviewService.submitReview(id, currentUserId(session), rating, comment);
            redirect.addFlashAttribute("success", "Thanks for your feedback!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    private Long currentUserId(HttpSession session) {
        return ((SessionUser) session.getAttribute(SessionUser.KEY)).getId();
    }
}
