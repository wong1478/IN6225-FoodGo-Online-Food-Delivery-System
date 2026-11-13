package com.in6225.fooddelivery.config;

import com.in6225.fooddelivery.entity.Role;
import com.in6225.fooddelivery.model.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs before every protected page.
 * - Not logged in  -> sent to the login page
 * - Wrong role     -> 403 "access denied" page
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SessionUser user = (SessionUser) request.getSession().getAttribute(SessionUser.KEY);
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        Role required = requiredRole(request.getRequestURI());
        if (user.getRole() != required) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private Role requiredRole(String path) {
        if (path.startsWith("/owner")) return Role.RESTAURANT_OWNER;
        if (path.startsWith("/admin")) return Role.ADMIN;
        return Role.CUSTOMER; // /cart and /orders
    }
}
