package com.fooddelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.repository.FoodItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodItemService {
    private final FoodItemRepository foodItemRepository;

    public List<FoodItem> getAllAvailableItems() {
        return foodItemRepository.findByAvailableTrue();
    }

    public List<FoodItem> searchItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllAvailableItems();
        }
        return foodItemRepository.searchAvailableItems(query.trim());
    }

    public List<FoodItem> getItemsByCategory(String category) {
        return foodItemRepository.findByCategory(category);
    }

    public Optional<FoodItem> getItemById(Long id) {
        return foodItemRepository.findById(id);
    }

    public FoodItem saveItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    public void deleteItem(Long id) {
        foodItemRepository.deleteById(id);
    }
}
