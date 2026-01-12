package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.*;
import com.example.cvcreator.ai.prompts.*;
import com.example.cvcreator.ai.utils.JsonCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CvGeneratorService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public CvGeneratorService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public AiCVDTO generateCv(AiCvRequestDTO request) {
        try {
            String userInput = request.getPrompt();
            String language = request.getLanguage();

            // ========== KROK 1: EKSTRAKCJA ==========
            System.out.println("=== KROK 1: Ekstrakcja danych ===");
            String extractPrompt = ExtractDataPrompt.build(userInput);
            String extractedRaw = geminiClient.generate(extractPrompt);
            String extractedClean = JsonCleaner.clean(extractedRaw);

            ExtractedDataDTO extracted = objectMapper.readValue(extractedClean, ExtractedDataDTO.class);

            // ========== KROK 2: GENEROWANIE CAŁEGO CV ==========
            System.out.println("\n=== KROK 2: Generowanie całego CV ===");
            String generateAllPrompt = buildGenerateAllPrompt(extracted, language);
            String cvRaw = geminiClient.generate(generateAllPrompt);
            String cvClean = JsonCleaner.clean(cvRaw);

            System.out.println("Wygenerowane CV:");
            System.out.println(cvClean);

            AiCVDTO cv = objectMapper.readValue(cvClean, AiCVDTO.class);

            // ========== KROK 3: WALIDACJA ==========
            validateAndClean(cv, extracted);

            // Dodaj UUID
            ensureIds(cv);

            // Ustaw kolejność sekcji - z requestu lub domyślną
            if (request.getSectionOrder() != null && !request.getSectionOrder().isEmpty()) {
                cv.setSectionOrder(request.getSectionOrder());
            } else {
                cv.setSectionOrder(Arrays.asList(
                        "summary",
                        "experience",
                        "education",
                        "skills",
                        "projects",
                        "interests"
                ));
            }

            System.out.println("\n=== CV WYGENEROWANE ===");
            return cv;

        } catch (Exception e) {
            System.err.println("Błąd podczas generowania CV: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nie udało się wygenerować CV", e);
        }
    }

    public AiCVDTO improveCv(AiCvRequestDTO request) {
        try {
            String newInput = request.getPrompt();
            String language = request.getLanguage();
            AiCVDTO existingCv = request.getCv();

            System.out.println("=== TRYB IMPROVE - Dodaję nowe dane do CV ===");

            // KROK 1: Ekstrakcja NOWYCH danych
            String extractPrompt = ExtractDataPrompt.build(newInput);
            String extractedRaw = geminiClient.generate(extractPrompt);
            String extractedClean = JsonCleaner.clean(extractedRaw);

            System.out.println("Nowe dane do dodania:");
            System.out.println(extractedClean);

            ExtractedDataDTO newData = objectMapper.readValue(extractedClean, ExtractedDataDTO.class);

            // KROK 2: Połącz istniejące CV z nowymi danymi
            String improvePrompt = buildImprovePrompt(existingCv, newData, language);
            String improvedRaw = geminiClient.generate(improvePrompt);
            String improvedClean = JsonCleaner.clean(improvedRaw);

            System.out.println("Ulepszone CV:");
            System.out.println(improvedClean);

            AiCVDTO improvedCv = objectMapper.readValue(improvedClean, AiCVDTO.class);

            // Zachowaj ID
            improvedCv.setId(existingCv.getId());

            // Ustaw kolejność sekcji - priorytet: request > istniejące CV > domyślna
            if (request.getSectionOrder() != null && !request.getSectionOrder().isEmpty()) {
                improvedCv.setSectionOrder(request.getSectionOrder());
            } else if (existingCv.getSectionOrder() != null && !existingCv.getSectionOrder().isEmpty()) {
                improvedCv.setSectionOrder(existingCv.getSectionOrder());
            } else {
                improvedCv.setSectionOrder(Arrays.asList(
                        "summary",
                        "experience",
                        "education",
                        "skills",
                        "projects",
                        "interests"
                ));
            }

            // Dodaj UUID dla nowych elementów
            ensureIds(improvedCv);

            return improvedCv;

        } catch (Exception e) {
            System.err.println("Błąd podczas ulepszania CV: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nie udało się ulepszyć CV", e);
        }
    }

    private String buildGenerateAllPrompt(ExtractedDataDTO extracted, String language) {
        return """
                Wygeneruj KOMPLETNE CV używając TYLKO tych wyekstrahowanych danych:

                DANE OSOBOWE:
                %s

                DOŚWIADCZENIE (surowe dane):
                %s

                WYKSZTAŁCENIE (surowe dane):
                %s

                UMIEJĘTNOŚCI (surowe dane):
                %s

                PROJEKTY (surowe dane):
                %s

                JĘZYKI (surowe dane):
                %s

                ZAINTERESOWANIA:
                %s

                ═══════════════════════════════════════════════
                ZASADY KRYTYCZNE - NAJWAŻNIEJSZE!!!
                ═══════════════════════════════════════════════

                ⛔ ABSOLUTNY ZAKAZ ZMYŚLANIA:
                1. Jeśli sekcja powyżej jest PUSTA ([], null, "BRAK DANYCH") → zwróć pustą listę []
                2. NIE DODAWAJ żadnych umiejętności, projektów, doświadczeń "od siebie"
                3. NIE GENERUJ przykładowych danych
                4. Jeśli użytkownik NIE podał umiejętności → skills: {"technical": [], "soft": []}
                5. Jeśli użytkownik NIE podał doświadczenia → experience: []
                6. Summary generuj TYLKO jeśli masz dane z experience/education/skills

                Język wyjścia: %s

                ZWRÓĆ TYLKO CZYSTY JSON (bez markdown, bez ```):

                {
                  "personalInfo": {
                    "firstName": "...",
                    "lastName": "...",
                    "email": null,
                    "phone": null,
                    "address": null,
                    "city": null,
                    "country": null,
                    "linkedin": null,
                    "github": null
                  },
                  "summary": null,
                  "experience": [],
                  "education": [],
                  "skills": {
                    "technical": [],
                    "soft": []
                  },
                  "projects": [],
                  "interests": []
                }
                """.formatted(
                toJson(extracted.getPersonalInfo()),
                extracted.getRawExperience().isEmpty() ? "BRAK DANYCH" : extracted.getRawExperience(),
                extracted.getRawEducation().isEmpty() ? "BRAK DANYCH" : extracted.getRawEducation(),
                extracted.getRawSkills().isEmpty() ? "BRAK DANYCH" : extracted.getRawSkills(),
                extracted.getRawProjects().isEmpty() ? "BRAK DANYCH" : extracted.getRawProjects(),
                extracted.getRawLanguages().isEmpty() ? "BRAK DANYCH" : extracted.getRawLanguages(),
                extracted.getRawInterests().isEmpty() ? "BRAK DANYCH" : extracted.getRawInterests(),
                language
        );
    }

    private String buildImprovePrompt(AiCVDTO existingCv, ExtractedDataDTO newData, String language) {
        return """
                ZADANIE: Połącz istniejące CV z nowymi danymi użytkownika.

                ISTNIEJĄCE CV:
                %s

                NOWE DANE DO DODANIA:
                Dane osobowe: %s
                Doświadczenie: %s
                Wykształcenie: %s
                Umiejętności: %s
                Projekty: %s
                Języki: %s

                ZASADY:
                1. ZACHOWAJ wszystkie istniejące dane
                2. DODAJ nowe informacje (nie duplikuj)
                3. Jeśli nowe dane uzupełniają istniejące (np. nowy email) → zaktualizuj
                4. NIE usuwaj niczego z istniejącego CV
                5. Połącz umiejętności (usuń duplikaty)
                6. Zaktualizuj summary uwzględniając nowe dane
                7. NIE DODAWAJ danych, których nie ma ani w istniejącym CV, ani w nowych danych

                PRZYKŁAD:
                - Istniejące: "firstName": "Jan", skills: ["Java"]
                - Nowe: "email": "jan@example.com", skills: ["Python"]
                - Wynik: "firstName": "Jan", "email": "jan@example.com", skills: ["Java", "Python"]

                Język: %s

                ZWRÓĆ TYLKO CZYSTY JSON (kompletne CV po połączeniu):
                """.formatted(
                toJson(existingCv),
                toJson(newData.getPersonalInfo()),
                newData.getRawExperience().isEmpty() ? "BRAK" : newData.getRawExperience(),
                newData.getRawEducation().isEmpty() ? "BRAK" : newData.getRawEducation(),
                newData.getRawSkills().isEmpty() ? "BRAK" : newData.getRawSkills(),
                newData.getRawProjects().isEmpty() ? "BRAK" : newData.getRawProjects(),
                newData.getRawLanguages().isEmpty() ? "BRAK" : newData.getRawLanguages(),
                language
        );
    }

    private void validateAndClean(AiCVDTO cv, ExtractedDataDTO extracted) {
        System.out.println("\n=== KROK 3: Walidacja danych ===");

        if (extracted.getRawSkills().isEmpty()) {
            System.out.println("⚠️ Brak umiejętności w input - czyszczę skills");
            cv.getSkills().setTechnical(new ArrayList<>());
            cv.getSkills().setSoft(new ArrayList<>());
        }

        if (extracted.getRawExperience().isEmpty()) {
            System.out.println("⚠️ Brak doświadczenia w input - czyszczę experience");
            cv.setExperience(new ArrayList<>());
        }

        if (extracted.getRawEducation().isEmpty()) {
            System.out.println("⚠️ Brak wykształcenia w input - czyszczę education");
            cv.setEducation(new ArrayList<>());
        }

        if (extracted.getRawProjects().isEmpty()) {
            System.out.println("⚠️ Brak projektów w input - czyszczę projects");
            cv.setProjects(new ArrayList<>());
        }

        if (extracted.getRawInterests().isEmpty()) {
            cv.setInterests(new ArrayList<>());
        }

        boolean hasAnyData = !extracted.getRawExperience().isEmpty()
                || !extracted.getRawSkills().isEmpty()
                || !extracted.getRawEducation().isEmpty()
                || !extracted.getRawProjects().isEmpty();

        if (!hasAnyData) {
            System.out.println("⚠️ Brak jakichkolwiek danych - usuwam summary");
            cv.setSummary(null);
        }
    }

    private void ensureIds(AiCVDTO cv) {
        if (cv.getId() == null) {
            cv.setId(UUID.randomUUID());
        }

        if (cv.getPersonalInfo() != null && cv.getPersonalInfo().getId() == null) {
            cv.getPersonalInfo().setId(UUID.randomUUID());
        }

        if (cv.getExperience() != null) {
            cv.getExperience().forEach(exp -> {
                if (exp.getId() == null) exp.setId(UUID.randomUUID());
            });
        }

        if (cv.getEducation() != null) {
            cv.getEducation().forEach(edu -> {
                if (edu.getId() == null) edu.setId(UUID.randomUUID());
            });
        }

        if (cv.getSkills() != null && cv.getSkills().getId() == null) {
            cv.getSkills().setId(UUID.randomUUID());
        }

        if (cv.getProjects() != null) {
            cv.getProjects().forEach(proj -> {
                if (proj.getId() == null) proj.setId(UUID.randomUUID());
            });
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}