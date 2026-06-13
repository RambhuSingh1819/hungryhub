package com.fooddelivery.service.ai;

import com.fooddelivery.dto.AiFoodSuggestionResponse;

public interface AiProvider {
    AiFoodSuggestionResponse getFoodSuggestion(String name, String category);
}
