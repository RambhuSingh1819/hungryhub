package com.fooddelivery.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fooddelivery.dto.AdminRegistrationRequest;
import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.repository.AdminRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.AdminService;
import com.fooddelivery.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserService userService;
    private final AdminService adminService;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Customer
        if (!userRepository.existsByEmail("user@hungryhub.com")) {
            UserRegistrationRequest req = new UserRegistrationRequest();
            req.setEmail("user@hungryhub.com");
            req.setFullName("Test Customer");
            req.setPassword("user123");
            req.setPhoneNumber("9876543210");
            req.setAddress("123 Food Street, Yumtown");
            userService.registerUser(req);
            System.out.println("✅ DatabaseInitializer: Registered test user 'user@hungryhub.com' (password: user123)");
        }

        // 2. Seed Admin
        if (!adminRepository.existsByEmail("rambhusingh1819@gmail.com")) {
            AdminRegistrationRequest req = new AdminRegistrationRequest();
            req.setEmail("rambhusingh1819@gmail.com");
            req.setFullName("Rambhu Singh");
            req.setPassword("admin123");
            req.setPhoneNumber("9999988888");
            Admin admin = adminService.registerAdmin(req);
            // Mark subscription as paid & active
            admin.setPaid(true);
            admin.setPlanType("MONTHLY");
            admin.setSubscriptionExpiry(LocalDate.now().plusMonths(6));
            adminRepository.save(admin);
            System.out.println("✅ DatabaseInitializer: Registered test admin 'rambhusingh1819@gmail.com' (password: admin123, subscription active)");
        }

        if (!adminRepository.existsByEmail("admin@hungryhub.com")) {
            AdminRegistrationRequest req = new AdminRegistrationRequest();
            req.setEmail("admin@hungryhub.com");
            req.setFullName("Default Admin");
            req.setPassword("admin123");
            req.setPhoneNumber("9876543211");
            Admin admin = adminService.registerAdmin(req);
            // Mark subscription as paid & active
            admin.setPaid(true);
            admin.setPlanType("MONTHLY");
            admin.setSubscriptionExpiry(LocalDate.now().plusMonths(6));
            adminRepository.save(admin);
            System.out.println("✅ DatabaseInitializer: Registered test admin 'admin@hungryhub.com' (password: admin123, subscription active)");
        }

        // 3. Seed Food Items
        if (foodItemRepository.count() == 0) {
            foodItemRepository.save(FoodItem.builder()
                    .name("Classic Veg Burger")
                    .description("Crispy vegetable patty with fresh lettuce, sliced tomatoes, onions, and creamy burger mayo.")
                    .price(new BigDecimal("129.00"))
                    .category("Burgers")
                    .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=600&auto=format&fit=crop")
                    .available(true)
                    .build());

            foodItemRepository.save(FoodItem.builder()
                    .name("Cheesy Margherita Pizza")
                    .description("Classic thin crust pizza topped with fresh tomato sauce, loaded with mozzarella cheese and fresh basil.")
                    .price(new BigDecimal("249.00"))
                    .category("Pizza")
                    .imageUrl("https://images.unsplash.com/photo-1604382355076-af4b0eb60143?q=80&w=600&auto=format&fit=crop")
                    .available(true)
                    .build());

            foodItemRepository.save(FoodItem.builder()
                    .name("Spicy Paneer Wrap")
                    .description("Soft tortilla wrap stuffed with grilled spicy paneer cubes, crunchy peppers, onions, and garlic sauce.")
                    .price(new BigDecimal("159.00"))
                    .category("Wraps")
                    .imageUrl("https://images.unsplash.com/photo-1562059390-a761a084768e?q=80&w=600&auto=format&fit=crop")
                    .available(true)
                    .build());

            foodItemRepository.save(FoodItem.builder()
                    .name("Cold Coffee")
                    .description("Chilled rich espresso blended with creamy milk, vanilla ice cream, and chocolate syrup drizzle.")
                    .price(new BigDecimal("99.00"))
                    .category("Drinks")
                    .imageUrl("https://images.unsplash.com/photo-1517701604599-bb29b565090c?q=80&w=600&auto=format&fit=crop")
                    .available(true)
                    .build());

            foodItemRepository.save(FoodItem.builder()
                    .name("Chocolate Lava Cake")
                    .description("Decadent individual chocolate cake with a warm, molten liquid chocolate center.")
                    .price(new BigDecimal("119.00"))
                    .category("Desserts")
                    .imageUrl("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?q=80&w=600&auto=format&fit=crop")
                    .available(true)
                    .build());

            System.out.println("✅ DatabaseInitializer: Seeded 5 initial food items into the database.");
        }

        // Fix Egg_roll image if it was created with a burger/placeholder image
        foodItemRepository.saveAll(
            foodItemRepository.findAll().stream()
                .filter(item -> "egg_roll".equalsIgnoreCase(item.getName()) || "egg roll".equalsIgnoreCase(item.getName()))
                .map(item -> {
                    item.setImageUrl("https://images.unsplash.com/photo-1606755962773-d324e0a13086?q=80&w=600&auto=format&fit=crop");
                    return item;
                })
                .toList()
        );
    }
}
