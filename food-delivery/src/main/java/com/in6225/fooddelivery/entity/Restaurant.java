package com.in6225.fooddelivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String cuisine;

    private String address;

    @Column(length = 1000)
    private String description;

    /** Whether the restaurant is currently accepting orders. */
    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    /** The restaurant owner (a user with role RESTAURANT_OWNER). */
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Restaurant() {
    }

    public Restaurant(String name, String cuisine, String address, String description, User owner) {
        this.name = name;
        this.cuisine = cuisine;
        this.address = address;
        this.description = description;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
