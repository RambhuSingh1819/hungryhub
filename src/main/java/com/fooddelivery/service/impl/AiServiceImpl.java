package com.fooddelivery.service.impl;

import org.springframework.stereotype.Service;

import com.fooddelivery.dto.AiFoodSuggestionRequest;
import com.fooddelivery.dto.AiFoodSuggestionResponse;
import com.fooddelivery.service.AiService;
import com.fooddelivery.service.ai.AiProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiProvider aiProvider;

    @Override
    public AiFoodSuggestionResponse generateFoodSuggestion(String name, String category) {
        return aiProvider.getFoodSuggestion(name, category);
    }

    @Override
    public AiFoodSuggestionResponse generateFoodSuggestion(AiFoodSuggestionRequest req) {
        return generateFoodSuggestion(req.getName(), req.getCategory());
    }
}
