package com.fooddelivery.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiFoodSuggestionResponse {
    private String description;
    private String imageUrl;
    private List<String> suggestions;
}
