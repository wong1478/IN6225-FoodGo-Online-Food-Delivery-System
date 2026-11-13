package com.in6225.fooddelivery.controller;

import com.in6225.fooddelivery.entity.FulfilmentType;
import com.in6225.fooddelivery.entity.MenuItem;
import com.in6225.fooddelivery.entity.Order;
import com.in6225.fooddelivery.model.Cart;
import com.in6225.fooddelivery.model.SessionUser;
import com.in6225.fooddelivery.service.OrderService;
import com.in6225.fooddelivery.service.RestaurantService;
import com.in6225.fooddelivery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Customer cart and checkout. Only CUSTOMER users get here (see AuthInterceptor). */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final UserService userService;

    public CartController(RestaurantService restaurantService, OrderService orderService, UserService userService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.KEY);
        model.addAttribute("cart", getCart(session));
        model.addAttribute("defaultAddress", userService.findById(user.getId()).getAddress());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long menuItemId, @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session, RedirectAttributes redirect) {
        MenuItem item = restaurantService.findMenuItem(menuItemId);
        try {
            getCart(session).add(item, Math.max(1, quantity));
            redirect.addFlashAttribute("success", item.getName() + " added to cart.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurants/" + item.getRestaurant().getId();
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long menuItemId, @RequestParam int quantity, HttpSession session) {
        getCart(session).updateQuantity(menuItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Long menuItemId, HttpSession session) {
        getCart(session).remove(menuItemId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        getCart(session).clear();
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam FulfilmentType fulfilmentType,
                           @RequestParam(required = false) String deliveryAddress,
                           @RequestParam(required = false) String paymentMethod,
                           @RequestParam(required = false) String cardNumber,
                           HttpSession session, RedirectAttributes redirect) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.KEY);
        Cart cart = getCart(session);
        try {
            Order order = orderService.placeOrder(user.getId(), cart, fulfilmentType, deliveryAddress,
                    paymentMethod, cardNumber);
            cart.clear();
            redirect.addFlashAttribute("success", "Order #" + order.getId() + " placed! You can track it below.");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /** Gets the cart from the session, creating an empty one the first time. */
    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(Cart.KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(Cart.KEY, cart);
        }
        return cart;
    }
}
