package com.in6225.fooddelivery.model;

import com.in6225.fooddelivery.entity.Role;
import com.in6225.fooddelivery.entity.User;

/**
 * The small piece of user info kept in the HTTP session after login.
 * (We don't keep the whole User entity in the session.)
 */
public class SessionUser {

    public static final String KEY = "loggedInUser";

    private final Long id;
    private final String name;
    private final Role role;

    public SessionUser(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    // Handy checks for the HTML templates, e.g. th:if="${currentUser.customer}"
    public boolean isCustomer() { return role == Role.CUSTOMER; }
    public boolean isOwner() { return role == Role.RESTAURANT_OWNER; }
    public boolean isAdmin() { return role == Role.ADMIN; }
}
