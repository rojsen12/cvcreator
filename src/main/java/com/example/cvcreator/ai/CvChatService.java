package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.*;
import com.example.cvcreator.ai.utils.JsonCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class CvChatService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public CvChatService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public SectionResponseDTO generateSection(SectionRequestDTO request) {
        try {
            String prompt = buildSectionPrompt(request);
            String rawResponse = geminiClient.generate(prompt);
            String cleanJson = JsonCleaner.clean(rawResponse);

            Object parsed = parseSection(request.getSectionType(), cleanJson);

            SectionResponseDTO response = new SectionResponseDTO();
            response.setSectionType(request.getSectionType());
            response.setGeneratedContent(parsed);
            response.setRawJson(cleanJson);
            response.setSuccess(true);
            response.setMessage("Sekcja wygenerowana pomyślnie");

            return response;

        } catch (Exception e) {
            SectionResponseDTO response = new SectionResponseDTO();
            response.setSectionType(request.getSectionType());
            response.setSuccess(false);
            response.setMessage("Błąd: " + e.getMessage());
            return response;
        }
    }

    private String buildSectionPrompt(SectionRequestDTO request) {
        String lang = request.getLanguage() != null ? request.getLanguage() : "pl";

        return switch (request.getSectionType()) {
            case "personalInfo" -> buildPersonalInfoPrompt(request.getUserInput(), lang);
            case "summary" -> buildSummaryPrompt(request.getUserInput(), lang);
            case "experience" -> buildExperiencePrompt(request.getUserInput(), lang);
            case "education" -> buildEducationPrompt(request.getUserInput(), lang);
            case "skills" -> buildSkillsPrompt(request.getUserInput(), lang);
            case "projects" -> buildProjectsPrompt(request.getUserInput(), lang);
            case "interests" -> buildInterestsPrompt(request.getUserInput(), lang);
            default -> throw new IllegalArgumentException("Nieznany typ sekcji: " + request.getSectionType());
        };
    }

    private Object parseSection(String sectionType, String json) throws Exception {
        return switch (sectionType) {
            case "personalInfo" -> objectMapper.readValue(json, AiPersonalInfoDTO.class);
            case "summary" -> objectMapper.readValue(json, SummaryWrapper.class).getSummary();
            case "experience" -> objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, AiExperienceDTO.class));
            case "education" -> objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, AiEducationDTO.class));
            case "skills" -> objectMapper.readValue(json, AiSkillsDTO.class);
            case "projects" -> objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, AiProjectDTO.class));
            case "interests" -> objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
            default -> json;
        };
    }

    // =============== PROMPTY DLA KAŻDEJ SEKCJI ===============

    private String buildPersonalInfoPrompt(String input, String lang) {
        return """
            Wyciągnij dane osobowe z tekstu użytkownika.
            
            TEKST: %s
            
            ZASADY:
            - Wyciągnij TYLKO to co podano
            - Jeśli czegoś brak, ustaw null
            - NIE wymyślaj danych
            
            Język: %s
            
            Zwróć TYLKO JSON:
            {
              "firstName": "...",
              "lastName": "...",
              "email": null,
              "phone": null,
              "address": null,
              "city": null,
              "country": null,
              "linkedin": null,
              "github": null
            }
            """.formatted(input, lang);
    }

    private String buildSummaryPrompt(String input, String lang) {
        return """
            Napisz profesjonalne podsumowanie zawodowe na podstawie opisu użytkownika.
            
            OPIS UŻYTKOWNIKA: %s
            
            ZASADY:
            - 2-3 zdania
            - Profesjonalny ton
            - Podkreśl kluczowe kompetencje
            - NIE wymyślaj informacji których nie ma w opisie
            
            Język: %s
            
            Zwróć TYLKO JSON:
            {"summary": "..."}
            """.formatted(input, lang);
    }

    private String buildExperiencePrompt(String input, String lang) {
        return """
            Przekształć opis doświadczenia zawodowego w ustrukturyzowany format CV.
            
            OPIS: %s
            
            ZASADY:
            - Każde stanowisko jako osobny obiekt
            - description jako lista punktów (achievements)
            - Używaj czasowników akcji
            - NIE dodawaj doświadczeń których użytkownik nie podał
            
            Język: %s
            
            Zwróć TYLKO JSON (lista):
            [
              {
                "title": "Stanowisko",
                "company": "Firma",
                "location": "Miasto",
                "startDate": "MM/YYYY",
                "endDate": "MM/YYYY lub null jeśli obecnie",
                "description": ["punkt 1", "punkt 2"]
              }
            ]
            """.formatted(input, lang);
    }

    private String buildEducationPrompt(String input, String lang) {
        return """
            Przekształć opis wykształcenia w format CV.
            
            OPIS: %s
            
            ZASADY:
            - Każda szkoła/uczelnia jako osobny obiekt
            - NIE wymyślaj danych
            
            Język: %s
            
            Zwróć TYLKO JSON (lista):
            [
              {
                "degree": "Stopień (np. Magister, Licencjat)",
                "fieldOfStudy": "Kierunek",
                "university": "Nazwa uczelni",
                "location": "Miasto",
                "startDate": "YYYY",
                "endDate": "YYYY",
                "description": []
              }
            ]
            """.formatted(input, lang);
    }

    private String buildSkillsPrompt(String input, String lang) {
        return """
            Wyciągnij umiejętności z opisu użytkownika.
            
            OPIS: %s
            
            ZASADY:
            - Podziel na techniczne i miękkie
            - TYLKO umiejętności które użytkownik faktycznie podał
            - NIE dodawaj umiejętności "od siebie"
            
            Język: %s
            
            Zwróć TYLKO JSON:
            {
              "technical": ["skill1", "skill2"],
              "soft": ["skill1", "skill2"]
            }
            """.formatted(input, lang);
    }

    private String buildProjectsPrompt(String input, String lang) {
        return """
            Przekształć opis projektów w format CV.
            
            OPIS: %s
            
            ZASADY:
            - Każdy projekt osobno
            - Wyciągnij użyte technologie
            - NIE wymyślaj projektów
            
            Język: %s
            
            Zwróć TYLKO JSON (lista):
            [
              {
                "name": "Nazwa projektu",
                "description": "Krótki opis",
                "technologies": ["tech1", "tech2"],
                "link": null
              }
            ]
            """.formatted(input, lang);
    }

    private String buildInterestsPrompt(String input, String lang) {
        return """
            Wyciągnij zainteresowania z opisu.
            
            OPIS: %s
            
            ZASADY:
            - Tylko wymienione zainteresowania
            - NIE dodawaj swoich
            
            Język: %s
            
            Zwróć TYLKO JSON (lista stringów):
            ["zainteresowanie1", "zainteresowanie2"]
            """.formatted(input, lang);
    }

    // Helper class dla summary
    @lombok.Data
    private static class SummaryWrapper {
        private String summary;
    }
}