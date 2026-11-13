package com.in6225.fooddelivery.service;

import com.in6225.fooddelivery.entity.*;
import com.in6225.fooddelivery.model.Cart;
import com.in6225.fooddelivery.model.CartItem;
import com.in6225.fooddelivery.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All order rules live here:
 * customer places/cancels -> restaurant accepts/rejects -> marks ready -> marks delivered/picked up.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantService restaurantService;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository, RestaurantService restaurantService, UserService userService) {
        this.orderRepository = orderRepository;
        this.restaurantService = restaurantService;
        this.userService = userService;
    }

    // ---------- Queries ----------

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    public List<Order> findByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> findByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    // ---------- Customer actions ----------

    /**
     * Turns the session cart into a saved order and processes the (simulated) payment.
     * No real card details are stored - only the last 4 digits.
     */
    @Transactional
    public Order placeOrder(Long customerId, Cart cart, FulfilmentType fulfilmentType, String deliveryAddress,
                            String paymentMethod, String cardNumber) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }
        if (fulfilmentType == null) {
            throw new IllegalArgumentException("Please choose delivery or in-store pickup.");
        }
        boolean delivery = fulfilmentType == FulfilmentType.DELIVERY;
        if (delivery && (deliveryAddress == null || deliveryAddress.isBlank())) {
            throw new IllegalArgumentException("Please enter a delivery address.");
        }

        Restaurant restaurant = restaurantService.findById(cart.getRestaurantId());
        if (!restaurant.isOpen()) {
            throw new IllegalStateException(restaurant.getName() + " is closed right now.");
        }

        Order order = new Order();
        order.setCustomer(userService.findById(customerId));
        order.setRestaurant(restaurant);
        order.setFulfilmentType(fulfilmentType);
        order.setDeliveryAddress(delivery ? deliveryAddress.trim() : null);

        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = restaurantService.findMenuItem(cartItem.getMenuItemId());
            if (!menuItem.isAvailable()) {
                throw new IllegalStateException(menuItem.getName() + " is no longer available.");
            }
            order.addItem(new OrderItem(menuItem, cartItem.getQuantity()));
        }

        // Prices are taken from the database, not from the cart. Pickup has no delivery fee.
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = delivery ? Cart.DELIVERY_FEE : BigDecimal.ZERO;
        order.setSubtotal(subtotal);
        order.setDeliveryFee(fee);
        order.setTotalAmount(subtotal.add(fee));

        processPayment(order, paymentMethod, cardNumber);
        return orderRepository.save(order);
    }

    /** Simulated payment gateway. */
    private void processPayment(Order order, String paymentMethod, String cardNumber) {
        if ("CARD".equals(paymentMethod)) {
            String digits = cardNumber == null ? "" : cardNumber.replaceAll("[\\s-]", "");
            if (!digits.matches("\\d{16}")) {
                throw new IllegalArgumentException("Please enter a valid 16-digit card number.");
            }
            order.setPaymentMethod("CARD (**** " + digits.substring(12) + ")");
            order.setPaymentStatus("PAID");
        } else if ("CASH".equals(paymentMethod)) {
            order.setPaymentMethod("CASH");
            order.setPaymentStatus("PENDING");
        } else {
            throw new IllegalArgumentException("Please choose a payment method.");
        }
    }

    @Transactional
    public void cancelByCustomer(Long orderId, Long customerId) {
        Order order = findById(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("This is not your order.");
        }
        requireStatus(order, OrderStatus.PLACED, "Orders can only be cancelled before the restaurant accepts them.");
        cancel(order);
    }

    // ---------- Restaurant owner actions ----------

    @Transactional
    public void acceptOrder(Long orderId, Long ownerId) {
        Order order = findOwnerOrder(orderId, ownerId);
        requireStatus(order, OrderStatus.PLACED, "Only new orders can be accepted.");
        order.setStatus(OrderStatus.PREPARING);
    }

    @Transactional
    public void rejectOrder(Long orderId, Long ownerId) {
        Order order = findOwnerOrder(orderId, ownerId);
        requireStatus(order, OrderStatus.PLACED, "Only new orders can be rejected.");
        cancel(order);
    }

    /** Food is ready: send it out (delivery) or wait for the customer (pickup). */
    @Transactional
    public void markReady(Long orderId, Long ownerId) {
        Order order = findOwnerOrder(orderId, ownerId);
        requireStatus(order, OrderStatus.PREPARING, "Only orders being prepared can be marked ready.");
        order.setStatus(order.isPickup() ? OrderStatus.READY_FOR_PICKUP : OrderStatus.OUT_FOR_DELIVERY);
    }

    /** The customer has received the food (delivered or collected in store). */
    @Transactional
    public void markCompleted(Long orderId, Long ownerId) {
        Order order = findOwnerOrder(orderId, ownerId);
        OrderStatus expected = order.isPickup() ? OrderStatus.READY_FOR_PICKUP : OrderStatus.OUT_FOR_DELIVERY;
        requireStatus(order, expected, "This order is not ready to be completed.");

        order.setStatus(order.isPickup() ? OrderStatus.PICKED_UP : OrderStatus.DELIVERED);
        order.setCompletedAt(LocalDateTime.now());
        if ("PENDING".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID"); // cash collected
        }
    }

    private Order findOwnerOrder(Long orderId, Long ownerId) {
        Order order = findById(orderId);
        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("This order is not for your restaurant.");
        }
        return order;
    }

    // ---------- Helpers ----------

    private void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        if ("PAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("REFUNDED");
        }
    }

    private void requireStatus(Order order, OrderStatus expected, String message) {
        if (order.getStatus() != expected) {
            throw new IllegalStateException(message);
        }
    }
}
