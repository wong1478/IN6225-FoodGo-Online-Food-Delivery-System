package com.in6225.fooddelivery.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "order" is a reserved word in SQL and JPQL, so the table is called "orders"
 * and the JPA entity name is "FoodOrder" (the Java class is still Order).
 */
@Entity(name = "FoodOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /** DELIVERY (restaurant delivers) or PICKUP (customer collects in store). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfilmentType fulfilmentType = FulfilmentType.DELIVERY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    /** Only filled in for delivery orders. */
    private String deliveryAddress;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** e.g. "CARD (**** 4242)" or "CASH" */
    @Column(nullable = false)
    private String paymentMethod;

    /** PAID, PENDING (cash, paid on delivery/pickup) or REFUNDED */
    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** When the order was delivered or picked up. */
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    // ---------- Helpers used by the service and the web pages (not database columns) ----------

    public boolean isPickup() {
        return fulfilmentType == FulfilmentType.PICKUP;
    }

    /** Delivered or picked up. */
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.PICKED_UP;
    }

    /** The steps shown in the tracking bar, depending on delivery or pickup. */
    public List<OrderStatus> getTrackingSteps() {
        if (isPickup()) {
            return List.of(OrderStatus.PLACED, OrderStatus.PREPARING,
                    OrderStatus.READY_FOR_PICKUP, OrderStatus.PICKED_UP);
        }
        return List.of(OrderStatus.PLACED, OrderStatus.PREPARING,
                OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED);
    }

    /** Position of the current status in the tracking bar (0 = placed). */
    public int getCurrentStepIndex() {
        return getTrackingSteps().indexOf(status);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public FulfilmentType getFulfilmentType() { return fulfilmentType; }
    public void setFulfilmentType(FulfilmentType fulfilmentType) { this.fulfilmentType = fulfilmentType; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
