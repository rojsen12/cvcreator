package com.example.cvcreator;

import com.example.cvcreator.ai.AiCvService;
import com.example.cvcreator.ai.GeminiClient;
import com.example.cvcreator.ai.dto.SectionRequest;
import com.example.cvcreator.ai.dto.SectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AiCvService - Testy integracyjne")
class AiCvServiceIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public GeminiClient geminiClient() {
            return mock(GeminiClient.class);
        }
    }

    @Autowired
    private AiCvService aiCvService;

    @Autowired
    private GeminiClient geminiClient;

    @Test
    @DisplayName("Integracja - kompletny flow generowania sekcji SUMMARY")
    void shouldGenerateSummarySection_withRealPromptFactory() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("Napisz krótkie podsumowanie dla Java Developera")
                .currentText("Stare podsumowanie")
                .language("pl")
                .build();

        String aiResponse = "[CONTENT]**Doświadczony Java Developer** z pasją do tworzenia skalowalnych aplikacji.[/CONTENT]" +
                "[MESSAGE]Stworzyłem profesjonalne podsumowanie dla Twojego CV![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent())
                .contains("Doświadczony Java Developer")
                .contains("skalowalnych aplikacji");
        assertThat(response.getMessage()).contains("Stworzyłem profesjonalne podsumowanie");
    }

    @Test
    @DisplayName("Integracja - generowanie sekcji EXPERIENCE z wieloma punktami")
    void shouldGenerateExperienceSection_withMultipleBulletPoints() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("EXPERIENCE")
                .userMessage("Dodaj doświadczenie jako Senior Java Developer")
                .currentText("")
                .language("pl")
                .build();

        String aiResponse = "[CONTENT]**Senior Java Developer** - Tech Corp\n" +
                "- Rozwój mikroserwisów w Spring Boot\n" +
                "- Optymalizacja wydajności aplikacji\n" +
                "- Code review i mentoring juniorów[/CONTENT]" +
                "[MESSAGE]Dodałem doświadczenie Senior Developer![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent())
                .contains("Senior Java Developer")
                .contains("Spring Boot")
                .contains("mikroserwisów")
                .contains("Code review");
        assertThat(response.getMessage()).isEqualTo("Dodałem doświadczenie Senior Developer!");
    }

    @Test
    @DisplayName("Integracja - generowanie SKILLS z kategoryzacją")
    void shouldGenerateSkillsSection_withCategories() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SKILLS")
                .userMessage("Dodaj umiejętności techniczne Java developera")
                .currentText("Java, Spring")
                .language("pl")
                .build();

        String aiResponse = "[CONTENT]**Języki programowania:** Java, Kotlin, SQL\n" +
                "**Frameworki:** Spring Boot, Hibernate, JPA\n" +
                "**Narzędzia:** Docker, Git, Maven[/CONTENT]" +
                "[MESSAGE]Dodałem szczegółowe umiejętności techniczne![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent())
                .contains("Języki programowania")
                .contains("Frameworki")
                .contains("Narzędzia")
                .contains("Spring Boot")
                .contains("Docker");
    }

    @Test
    @DisplayName("Integracja - obsługa odpowiedzi AI ze złym formatowaniem")
    void shouldHandleResponse_withMalformedTags() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("EDUCATION")
                .userMessage("Dodaj edukację")
                .currentText("Poprzednia edukacja")
                .build();

        // AI zwróciło odpowiedź bez poprawnych tagów
        String malformedResponse = "Jakiś tekst bez tagów CONTENT i MESSAGE";

        when(geminiClient.generate(anyString())).thenReturn(malformedResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        // Powinien zwrócić currentText bo nie znalazł tagów [CONTENT]
        assertThat(response.getContent()).isEqualTo("Poprzednia edukacja");
        // Domyślna wiadomość
        assertThat(response.getMessage()).isEqualTo("Stworzyłem propozycję. Sprawdź czy jest OK!");
    }

    @Test
    @DisplayName("Integracja - tworzenie sekcji od zera (bez currentText)")
    void shouldCreateSection_fromScratch() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("PROJECTS")
                .userMessage("Stwórz sekcję projektów dla e-commerce")
                .currentText(null)
                .language("pl")
                .build();

        String aiResponse = "[CONTENT]**System E-commerce**\n" +
                "Platforma sprzedażowa obsługująca 10k użytkowników dziennie\n" +
                "- Java 17, Spring Boot\n" +
                "- PostgreSQL, Redis\n" +
                "- Docker, Kubernetes[/CONTENT]" +
                "[MESSAGE]Stworzyłem sekcję projektów od podstaw![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent())
                .contains("System E-commerce")
                .contains("10k użytkowników")
                .contains("Spring Boot")
                .contains("Kubernetes");
        assertThat(response.getMessage()).contains("od podstaw");
    }

    @Test
    @DisplayName("Integracja - aktualizacja istniejącej sekcji")
    void shouldUpdateExistingSection_withUserFeedback() {
        // Given
        String existingContent = "**Java Developer** z 2 letnim doświadczeniem";

        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("Zmień na 5 lat doświadczenia i dodaj technologie")
                .currentText(existingContent)
                .language("pl")
                .build();

        String aiResponse = "[CONTENT]**Java Developer** z 5 letnim doświadczeniem w Spring Boot, " +
                "Hibernate i architekturze mikroserwisów.[/CONTENT]" +
                "[MESSAGE]Zaktualizowałem doświadczenie i dodałem technologie![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent())
                .contains("5 letnim doświadczeniem")
                .contains("Spring Boot")
                .contains("Hibernate")
                .contains("mikroserwisów");
        assertThat(response.getMessage()).contains("Zaktualizowałem");
    }

    @Test
    @DisplayName("Integracja - obsługa wielojęzyczności (EN)")
    void shouldGenerateSection_inEnglish() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("Write professional summary for Senior Developer")
                .currentText("")
                .language("en")
                .build();

        String aiResponse = "[CONTENT]**Senior Software Engineer** with 7+ years of experience in " +
                "building scalable enterprise applications.[/CONTENT]" +
                "[MESSAGE]I've created a professional summary for you![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent())
                .contains("Senior Software Engineer")
                .contains("7+ years")
                .contains("enterprise applications");
        assertThat(response.getMessage()).contains("I've created");
    }

    @Test
    @DisplayName("Integracja - czyszczenie nadmiarowego formatowania markdown")
    void shouldCleanMarkdownFormatting_fromAiResponse() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("Napisz podsumowanie")
                .build();

        String aiResponse = "[CONTENT]***Wyróżniony*** programista z ***dużym*** doświadczeniem " +
                "w **Java** i **Spring**.[/CONTENT]" +
                "[MESSAGE]Gotowe![/MESSAGE]";

        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        // *** powinno być zamienione na **
        assertThat(response.getContent())
                .contains("**Wyróżniony**")
                .contains("**dużym**")
                .doesNotContain("***");
    }

    @Test
    @DisplayName("Integracja - obsługa bardzo długiej odpowiedzi AI")
    void shouldHandleLongAiResponse_successfully() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("EXPERIENCE")
                .userMessage("Dodaj szczegółowe doświadczenie")
                .build();

        StringBuilder longContent = new StringBuilder("[CONTENT]");
        for (int i = 1; i <= 10; i++) {
            longContent.append("**Projekt ").append(i).append("**\n");
            longContent.append("- Szczegół A dla projektu ").append(i).append("\n");
            longContent.append("- Szczegół B dla projektu ").append(i).append("\n");
            longContent.append("- Szczegół C dla projektu ").append(i).append("\n\n");
        }
        longContent.append("[/CONTENT][MESSAGE]Dodałem 10 projektów![/MESSAGE]");

        when(geminiClient.generate(anyString())).thenReturn(longContent.toString());

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent())
                .contains("Projekt 1")
                .contains("Projekt 10")
                .contains("Szczegół A")
                .contains("Szczegół B")
                .contains("Szczegół C");
        assertThat(response.getMessage()).isEqualTo("Dodałem 10 projektów!");
    }

    @Test
    @DisplayName("Integracja - walidacja że puste userMessage nie wywołuje AI")
    void shouldNotCallAi_whenUserMessageIsEmpty() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("")
                .currentText("Existing content")
                .build();

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        // GeminiClient nie powinien być wywołany (nie mockujemy w tym teście)
        assertThat(response.getContent()).isEqualTo("Existing content");
        assertThat(response.getMessage()).isEqualTo("Hej! Napisz coś, żebym mógł Ci pomóc 😊");
    }
}