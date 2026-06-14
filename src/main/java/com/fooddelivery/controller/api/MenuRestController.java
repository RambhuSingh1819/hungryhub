package com.fooddelivery.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.service.FoodItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuRestController {

    private final FoodItemService foodItemService;

    @GetMapping
    public ResponseEntity<List<FoodItem>> getMenu(@RequestParam(value = "search", required = false) String search) {
        List<FoodItem> items;
        if (search != null && !search.isBlank()) {
            items = foodItemService.searchItems(search);
        } else {
            items = foodItemService.getAllAvailableItems();
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodItem> getItemById(@PathVariable Long id) {
        return foodItemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FoodItem>> getPopular(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<FoodItem> items = foodItemService.getTopPopularItems(limit);
        return ResponseEntity.ok(items);
    }
}
