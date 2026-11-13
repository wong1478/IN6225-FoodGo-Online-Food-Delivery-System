package com.in6225.fooddelivery.config;

import com.in6225.fooddelivery.model.Cart;
import com.in6225.fooddelivery.model.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds the logged-in user and cart count to every page (used by the navbar). */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionUser.KEY);
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(Cart.KEY);
        return cart == null ? 0 : cart.getTotalQuantity();
    }
}
