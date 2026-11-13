package com.in6225.fooddelivery.model;

import java.math.BigDecimal;

/** One line in the shopping cart. */
public class CartItem {

    private final Long menuItemId;
    private final String name;
    private final BigDecimal unitPrice;
    private int quantity;

    public CartItem(Long menuItemId, String name, BigDecimal unitPrice, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
