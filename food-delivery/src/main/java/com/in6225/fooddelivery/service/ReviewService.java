package com.in6225.fooddelivery.service;

import com.in6225.fooddelivery.entity.Order;
import com.in6225.fooddelivery.entity.Review;
import com.in6225.fooddelivery.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;

    public ReviewService(ReviewRepository reviewRepository, OrderService orderService) {
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
    }

    public Review submitReview(Long orderId, Long customerId, int rating, String comment) {
        Order order = orderService.findById(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("This is not your order.");
        }
        if (!order.isCompleted()) {
            throw new IllegalStateException("You can only review an order after it is delivered or picked up.");
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("You have already reviewed this order.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        Review review = new Review();
        review.setOrder(order);
        review.setCustomer(order.getCustomer());
        review.setRestaurant(order.getRestaurant());
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    public Optional<Review> findByOrder(Long orderId) {
        return reviewRepository.findByOrderId(orderId);
    }

    public List<Review> findByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    /** Average star rating, or null if there are no reviews yet. */
    public Double averageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }
}
