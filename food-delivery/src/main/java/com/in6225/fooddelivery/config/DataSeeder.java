package com.in6225.fooddelivery.config;

import com.in6225.fooddelivery.entity.MenuItem;
import com.in6225.fooddelivery.entity.Restaurant;
import com.in6225.fooddelivery.entity.Role;
import com.in6225.fooddelivery.entity.User;
import com.in6225.fooddelivery.repository.MenuItemRepository;
import com.in6225.fooddelivery.repository.RestaurantRepository;
import com.in6225.fooddelivery.repository.UserRepository;
import com.in6225.fooddelivery.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fills an empty database with demo accounts, restaurants and menus
 * so the app can be demonstrated straight away. Runs once at start-up
 * and does nothing if users already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public DataSeeder(UserRepository userRepository, UserService userService,
                      RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // Admin can't self-register, so it is created with the same hashing via a normal user + role change
        User admin = userService.register("Admin", "admin@foodgo.com", "admin123", "60000000", "HQ", Role.CUSTOMER);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        userService.register("Customer 1", "Customer1@foodgo.com", "pass123", "91234567",
                "test address, Singapore 12345", Role.CUSTOMER);

        User owner1 = userService.register("owner1", "owner1@foodgo.com", "pass123", "81112222", "", Role.RESTAURANT_OWNER);
        User owner2 = userService.register("owner2", "owner2@foodgo.com", "pass123", "83334444", "", Role.RESTAURANT_OWNER);
        User owner3 = userService.register("owner3", "owner3@foodgo.com", "pass123", "85556666", "", Role.RESTAURANT_OWNER);

        Restaurant wok = restaurantRepository.save(new Restaurant("Golden Wok", "Chinese",
                "Owner address 1", "Hawker favourites cooked to order.", owner1));
        Restaurant pasta = restaurantRepository.save(new Restaurant("Pasta Corner", "Italian",
                "Owner address 2", "Fresh pasta and wood-fired pizza.", owner2));
        Restaurant sushi = restaurantRepository.save(new Restaurant("Sakura Sushi", "Japanese",
                "Owner address 3", "Sushi, bento and ramen.", owner3));

        addItem(wok, "Chicken Rice", "Steamed chicken with fragrant rice", "5.50");
        addItem(wok, "Char Kway Teow", "Stir-fried flat noodles with prawns", "6.50");
        addItem(wok, "Sweet and Sour Pork", "Crispy pork in tangy sauce", "8.00");
        addItem(wok, "Iced Lemon Tea", "Freshly brewed", "2.00");

        addItem(pasta, "Spaghetti Carbonara", "Egg, pecorino and bacon", "14.90");
        addItem(pasta, "Margherita Pizza", "Tomato, mozzarella and basil", "16.00");
        addItem(pasta, "Tiramisu", "Classic coffee dessert", "7.50");

        addItem(sushi, "Salmon Sushi Set", "8 pieces of salmon nigiri", "15.00");
        addItem(sushi, "Chicken Teriyaki Bento", "With rice, salad and miso soup", "12.50");
        addItem(sushi, "Tonkotsu Ramen", "Pork broth ramen with egg", "13.80");
    }

    private void addItem(Restaurant restaurant, String name, String description, String price) {
        menuItemRepository.save(new MenuItem(name, description, new BigDecimal(price), restaurant));
    }
}
