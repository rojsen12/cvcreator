package com.example.cvcreator.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient implements LlmClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.url}") String baseUrl,
            ObjectMapper mapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.mapper = mapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String generate(String prompt) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String url = "%s/%s:generateContent?key=%s".formatted(cleanBaseUrl, model, apiKey);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractText(response.getBody());

        } catch (HttpClientErrorException e) {
            System.err.println("--- BŁĄD GOOGLE API ---");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Body: " + e.getResponseBodyAsString());
            System.err.println("URL (bez klucza): " + url.split("\\?")[0]);
            System.err.println("-----------------------");
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during AI generation", e);
        }
    }

    private String extractText(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");

            if (candidates.isMissingNode() || candidates.isEmpty()) {
                return "AI nie zwróciło żadnej odpowiedzi. Sprawdź filtry bezpieczeństwa Google.";
            }

            return candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas parsowania JSON od Google: " + responseJson, e);
        }
    }
}