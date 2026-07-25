package com.fooddelivery.service;

import java.util.List;
import java.util.Optional;

import com.fooddelivery.entity.FoodItem;

public interface FoodItemService {
    List<FoodItem> getAllAvailableItems();
    List<FoodItem> searchItems(String query);
    List<FoodItem> getItemsByCategory(String category);
    Optional<FoodItem> getItemById(Long id);
    FoodItem saveItem(FoodItem foodItem);
    void deleteItem(Long id);
    List<FoodItem> getTopPopularItems(int limit);
}
