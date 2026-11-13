package com.in6225.fooddelivery.controller;

import com.in6225.fooddelivery.entity.Restaurant;
import com.in6225.fooddelivery.entity.Review;
import com.in6225.fooddelivery.service.RestaurantService;
import com.in6225.fooddelivery.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Public pages: anyone can browse restaurants and menus. */
@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    public RestaurantController(RestaurantService restaurantService, ReviewService reviewService) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String listRestaurants(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("restaurants", restaurantService.search(q));
        model.addAttribute("q", q);
        return "restaurants";
    }

    @GetMapping("/restaurants/{id}")
    public String restaurantDetail(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantService.findById(id);
        List<Review> reviews = reviewService.findByRestaurant(id);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menuItems", restaurantService.getAvailableMenu(id));
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", reviewService.averageRating(reviews));
        return "restaurant";
    }
}
