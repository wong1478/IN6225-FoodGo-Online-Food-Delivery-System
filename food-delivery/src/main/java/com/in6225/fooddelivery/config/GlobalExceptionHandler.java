package com.in6225.fooddelivery.config;

import com.in6225.fooddelivery.model.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Shows a friendly error page (e.g. "Restaurant not found.") instead of a stack trace. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handle(RuntimeException e, HttpSession session, Model model) {
        model.addAttribute("message", e.getMessage());
        model.addAttribute("currentUser", session.getAttribute(SessionUser.KEY));
        model.addAttribute("cartCount", 0);
        return "error/general";
    }
}
