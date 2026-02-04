package com.fooddelivery.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.dto.AiFoodSuggestionRequest;
import com.fooddelivery.dto.AiFoodSuggestionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.base-url}")
    private String baseUrl;

    @Value("${openai.chat.model}")
    private String chatModel;

    @Value("${openai.image.model}")
    private String imageModel;

    private static final String UPLOAD_ROOT = "uploads/food"; // reserved for future local saves

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ===========================
    // MAIN SUGGESTION FUNCTION
    // ===========================
    public AiFoodSuggestionResponse generateFoodSuggestion(String name, String category) {

        try {
            String systemPrompt = """
                You are helping generate menu content for a food delivery admin panel.
                Respond ONLY in JSON with:

                {
                  "description": "short tasty description",
                  "variants": ["variant1", "variant2"],
                  "imagePrompt": "image generation prompt"
                }
                """;

            String userContent = "Food name: " + name + " | Category: " + category;

            // 1. Call ChatGPT (text) and get ONLY the JSON content string
            String assistantJsonString = callChatGPT(systemPrompt, userContent);

            // Parse the assistant's JSON string
            JsonNode json = objectMapper.readTree(assistantJsonString);

            String description = json.path("description").asText(null);
            String imagePrompt = json.path("imagePrompt").asText(null);

            List<String> variants = null;
            if (json.has("variants") && json.path("variants").isArray()) {
                variants = objectMapper.convertValue(
                        json.path("variants"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            }

            if (description == null || description.isBlank()) {
                description = "A delicious " + name + " made with premium fresh ingredients.";
            }

            if (imagePrompt == null || imagePrompt.isBlank()) {
                imagePrompt = "High quality photo of " + name;
            }

            if (variants == null || variants.isEmpty()) {
                variants = Arrays.asList("Cheese " + name, "Spicy " + name);
            }

            // 2. Try image generation
            String imageUrl = generateImageAndSave(imagePrompt, name);

            return AiFoodSuggestionResponse.builder()
                    .description(description)
                    .imageUrl(imageUrl == null ? "" : imageUrl)
                    .suggestions(variants)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            // FINAL FALLBACK WHEN ANYTHING FAILS
            return AiFoodSuggestionResponse.builder()
                    .description("Tasty " + name + " prepared freshly and perfectly balanced.")
                    .imageUrl("")
                    .suggestions(Arrays.asList("Cheese " + name, "Spicy " + name))
                    .build();
        }
    }

    public AiFoodSuggestionResponse generateFoodSuggestion(AiFoodSuggestionRequest req) {
        return generateFoodSuggestion(req.getName(), req.getCategory());
    }

    // ===========================
    // CALL CHAT COMPLETIONS
    // ===========================
    /**
     * Calls OpenAI chat API and returns ONLY the assistant's content string,
     * which should itself be JSON (because we instructed it that way).
     */
    private String callChatGPT(String systemPrompt, String userPrompt) {
        try {
            String url = baseUrl + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                    {
                      "model": "%s",
                      "messages": [
                        {"role": "system", "content": %s},
                        {"role": "user", "content": %s}
                      ]
                    }
                    """.formatted(chatModel, escape(systemPrompt), escape(userPrompt));

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate().exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("❌ OpenAI Chat Error Code: " + response.getStatusCode());
                System.err.println("❌ Error Body: " + response.getBody());
                return "{}"; // force fallback
            }

            String responseBody = response.getBody();
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                System.err.println("❌ No choices returned from OpenAI chat");
                return "{}";
            }

            String content = choices.get(0).path("message").path("content").asText("{}");
            if (content == null || content.isBlank()) {
                System.err.println("❌ Empty content from OpenAI chat");
                return "{}";
            }

            // content is expected to be JSON string like:
            // {"description": "...", "variants": [...], "imagePrompt": "..."}
            return content;

        } catch (Exception ex) {
            System.err.println("❌ ChatGPT Exception: " + ex.getMessage());
            return "{}"; // fallback
        }
    }

    // ===========================
    // IMAGE GENERATION (REMOTE URL)
    // ===========================
    private String generateImageAndSave(String prompt, String itemName) {
        try {
            String url = baseUrl + "/images/generations";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                    {
                      "model": "%s",
                      "prompt": %s,
                      "size": "1024x1024",
                      "n": 1
                    }
                    """.formatted(imageModel, escape(prompt));

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate().exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("❌ OpenAI Image Error: " + response.getStatusCode());
                System.err.println("❌ Response: " + response.getBody());
                return "";
            }

            JsonNode json = objectMapper.readTree(response.getBody());
            JsonNode data = json.path("data");
            if (!data.isArray() || data.isEmpty()) {
                System.err.println("❌ OpenAI Image: no data array");
                return "";
            }

            String remoteUrl = data.get(0).path("url").asText("");
            System.out.println("✅ Generated image URL: " + remoteUrl);

            if (remoteUrl == null || remoteUrl.isBlank()) {
                return "";
            }

            // For now, just return OpenAI's URL directly
            return remoteUrl;

            // If later you want to store locally again:
            // return downloadAndSaveImage(remoteUrl, slugify(itemName));

        } catch (Exception e) {
            System.err.println("❌ Image generation failed: " + e.getMessage());
            return "";
        }
    }

    // ===========================
    // OPTIONAL: LOCAL IMAGE SAVE
    // ===========================
    private String downloadAndSaveImage(String url, String slug) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_ROOT));

        String fileName = slug + "-" + System.currentTimeMillis() + ".png";
        Path targetPath = Paths.get(UPLOAD_ROOT).resolve(fileName);

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/food/" + fileName;
    }

    private String slugify(String text) {
        if (text == null) {
            return "item";
        }
        return text.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private String escape(String text) {
        if (text == null) {
			text = "";
		}
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }
}
