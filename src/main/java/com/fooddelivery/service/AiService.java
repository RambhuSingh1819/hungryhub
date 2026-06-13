package com.fooddelivery.service;

import com.fooddelivery.dto.AiFoodSuggestionRequest;
import com.fooddelivery.dto.AiFoodSuggestionResponse;

public interface AiService {
    AiFoodSuggestionResponse generateFoodSuggestion(String name, String category);
    AiFoodSuggestionResponse generateFoodSuggestion(AiFoodSuggestionRequest req);
}
