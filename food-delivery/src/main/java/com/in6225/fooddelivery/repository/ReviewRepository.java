package com.in6225.fooddelivery.repository;

import com.in6225.fooddelivery.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    Optional<Review> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}
