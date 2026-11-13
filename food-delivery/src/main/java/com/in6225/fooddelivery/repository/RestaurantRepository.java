package com.in6225.fooddelivery.repository;

import com.in6225.fooddelivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /** Search by restaurant name or cuisine, e.g. "pizza" or "japanese". */
    List<Restaurant> findByNameContainingIgnoreCaseOrCuisineContainingIgnoreCase(String name, String cuisine);

    Optional<Restaurant> findByOwnerId(Long ownerId);
}
