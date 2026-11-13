package com.in6225.fooddelivery.entity;

/**
 * Lifecycle of an order (the restaurant moves it forward):
 *   Delivery: PLACED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
 *   Pickup:   PLACED -> PREPARING -> READY_FOR_PICKUP -> PICKED_UP
 * An order can be CANCELLED while it is still PLACED.
 */
public enum OrderStatus {
    PLACED("Order placed"),
    PREPARING("Preparing"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    READY_FOR_PICKUP("Ready for pickup"),
    PICKED_UP("Picked up"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Bootstrap colour used for the status badge on the web pages. */
    public String getBadgeClass() {
        return switch (this) {
            case DELIVERED, PICKED_UP -> "bg-success";
            case CANCELLED -> "bg-secondary";
            case OUT_FOR_DELIVERY, READY_FOR_PICKUP -> "bg-primary";
            default -> "bg-warning text-dark";
        };
    }
}
