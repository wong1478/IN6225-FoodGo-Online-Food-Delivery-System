package com.in6225.fooddelivery.entity;

/** How the customer gets the food. */
public enum FulfilmentType {
    DELIVERY("Delivery"),
    PICKUP("In-store pickup");

    private final String label;

    FulfilmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
