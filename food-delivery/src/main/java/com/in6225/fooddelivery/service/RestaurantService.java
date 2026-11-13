package com.in6225.fooddelivery.service;

import com.in6225.fooddelivery.entity.MenuItem;
import com.in6225.fooddelivery.entity.Restaurant;
import com.in6225.fooddelivery.entity.User;
import com.in6225.fooddelivery.repository.MenuItemRepository;
import com.in6225.fooddelivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserService userService;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository,
                             UserService userService) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.userService = userService;
    }

    // ---------- Restaurants ----------

    public List<Restaurant> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return restaurantRepository.findAll();
        }
        return restaurantRepository.findByNameContainingIgnoreCaseOrCuisineContainingIgnoreCase(keyword, keyword);
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found."));
    }

    public Optional<Restaurant> findByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    /** Creates the owner's restaurant the first time, updates it afterwards. */
    public Restaurant saveOwnerRestaurant(Long ownerId, String name, String cuisine, String address,
                                          String description, boolean open) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name is required.");
        }
        Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId).orElseGet(() -> {
            Restaurant r = new Restaurant();
            User owner = userService.findById(ownerId);
            r.setOwner(owner);
            return r;
        });
        restaurant.setName(name.trim());
        restaurant.setCuisine(cuisine);
        restaurant.setAddress(address);
        restaurant.setDescription(description);
        restaurant.setOpen(open);
        return restaurantRepository.save(restaurant);
    }

    // ---------- Menu ----------

    public List<MenuItem> getAvailableMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrueOrderByNameAsc(restaurantId);
    }

    public List<MenuItem> getFullMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdOrderByNameAsc(restaurantId);
    }

    public MenuItem findMenuItem(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found."));
    }

    public MenuItem addMenuItem(Long ownerId, String name, String description, BigDecimal price) {
        Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalStateException("Set up your restaurant first."));
        validateMenuItem(name, price);
        return menuItemRepository.save(new MenuItem(name.trim(), description, price, restaurant));
    }

    public MenuItem updateMenuItem(Long ownerId, Long itemId, String name, String description,
                                   BigDecimal price, boolean available) {
        MenuItem item = findOwnedMenuItem(ownerId, itemId);
        validateMenuItem(name, price);
        item.setName(name.trim());
        item.setDescription(description);
        item.setPrice(price);
        item.setAvailable(available);
        return menuItemRepository.save(item);
    }

    public void toggleAvailability(Long ownerId, Long itemId) {
        MenuItem item = findOwnedMenuItem(ownerId, itemId);
        item.setAvailable(!item.isAvailable());
        menuItemRepository.save(item);
    }

    /** Makes sure owners can only edit items of their own restaurant. */
    public MenuItem findOwnedMenuItem(Long ownerId, Long itemId) {
        MenuItem item = findMenuItem(itemId);
        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("This menu item does not belong to your restaurant.");
        }
        return item;
    }

    private void validateMenuItem(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
    }
}
