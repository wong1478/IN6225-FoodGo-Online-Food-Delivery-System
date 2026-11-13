package com.in6225.fooddelivery.model;

import com.in6225.fooddelivery.entity.MenuItem;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shopping cart stored in the HTTP session (not in the database).
 * A cart can only hold items from ONE restaurant at a time.
 */
public class Cart {

    public static final String KEY = "cart";
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");

    private Long restaurantId;
    private String restaurantName;
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public void add(MenuItem menuItem, int quantity) {
        Long itemRestaurantId = menuItem.getRestaurant().getId();
        if (restaurantId != null && !restaurantId.equals(itemRestaurantId)) {
            throw new IllegalStateException("Your cart already has items from " + restaurantName
                    + ". Clear the cart first to order from another restaurant.");
        }
        restaurantId = itemRestaurantId;
        restaurantName = menuItem.getRestaurant().getName();

        CartItem existing = items.get(menuItem.getId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            items.put(menuItem.getId(), new CartItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity));
        }
    }

    public void updateQuantity(Long menuItemId, int quantity) {
        if (quantity <= 0) {
            remove(menuItemId);
        } else if (items.containsKey(menuItemId)) {
            items.get(menuItemId).setQuantity(quantity);
        }
    }

    public void remove(Long menuItemId) {
        items.remove(menuItemId);
        if (items.isEmpty()) {
            clear();
        }
    }

    public void clear() {
        items.clear();
        restaurantId = null;
        restaurantName = null;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public int getTotalQuantity() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.values().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDeliveryFee() {
        return isEmpty() ? BigDecimal.ZERO : DELIVERY_FEE;
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getDeliveryFee());
    }

    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
}
