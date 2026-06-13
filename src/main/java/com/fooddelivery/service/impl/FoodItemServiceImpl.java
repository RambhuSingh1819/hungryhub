package com.fooddelivery.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.service.FoodItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodItemServiceImpl implements FoodItemService {
    private final FoodItemRepository foodItemRepository;

    @Override
    @Cacheable(value = "foodItems", key = "'allAvailable'")
    public List<FoodItem> getAllAvailableItems() {
        return foodItemRepository.findByAvailableTrue();
    }

    @Override
    @Cacheable(value = "foodItems", key = "'search:' + #query")
    public List<FoodItem> searchItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllAvailableItems();
        }
        return foodItemRepository.searchAvailableItems(query.trim());
    }

    @Override
    @Cacheable(value = "foodItems", key = "'category:' + #category")
    public List<FoodItem> getItemsByCategory(String category) {
        return foodItemRepository.findByCategory(category);
    }

    @Override
    @Cacheable(value = "foodItems", key = "#id")
    public Optional<FoodItem> getItemById(Long id) {
        return foodItemRepository.findById(id);
    }

    @Override
    @CacheEvict(value = "foodItems", allEntries = true)
    public FoodItem saveItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    @Override
    @CacheEvict(value = "foodItems", allEntries = true)
    public void deleteItem(Long id) {
        foodItemRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "foodItems", key = "'popular:' + #limit")
    public List<FoodItem> getTopPopularItems(int limit) {
        return foodItemRepository.findTopPopularItems(org.springframework.data.domain.PageRequest.of(0, limit));
    }
}
