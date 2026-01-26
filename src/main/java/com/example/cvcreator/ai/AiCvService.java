package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.SectionRequest;
import com.example.cvcreator.ai.dto.SectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiCvService {

    private final GeminiClient geminiClient;
    private final PromptFactory promptFactory;

    public SectionResponse generateSection(SectionRequest request) {
        if (request.getUserMessage() == null || request.getUserMessage().trim().isEmpty()) {
            return new SectionResponse(
                    request.getCurrentText() != null ? request.getCurrentText() : "",
                    "Hej! Napisz coś, żebym mógł Ci pomóc 😊"
            );
        }

        String prompt = promptFactory.buildSectionPrompt(request);
        String aiResponse = geminiClient.generate(prompt);

        return parseResponse(aiResponse, request);
    }

    private SectionResponse parseResponse(String text, SectionRequest request) {
        String content = extractBetween(text, "[CONTENT]", "[/CONTENT]");
        String message = extractBetween(text, "[MESSAGE]", "[/MESSAGE]");

        if (content.isEmpty() || content.equals("(pusta - tworzysz od zera)")) {
            content = request.getCurrentText() != null ? request.getCurrentText() : "";
        }

        if (message.isEmpty()) {
            message = "Stworzyłem propozycję. Sprawdź czy jest OK!";
        }

        content = content
                .replaceAll("```.*?```", "")
                .replaceAll("\\*\\*\\*+", "**")
                .trim();

        return new SectionResponse(content, message);
    }

    private String extractBetween(String text, String start, String end) {
        try {
            int startIdx = text.indexOf(start);
            int endIdx = text.indexOf(end);

            if (startIdx == -1 || endIdx == -1) {
                return "";
            }

            return text.substring(startIdx + start.length(), endIdx).trim();
        } catch (Exception e) {
            return "";
        }
    }
}