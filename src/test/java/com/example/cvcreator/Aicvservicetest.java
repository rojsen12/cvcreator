package com.example.cvcreator;

import com.example.cvcreator.ai.AiCvService;
import com.example.cvcreator.ai.GeminiClient;
import com.example.cvcreator.ai.PromptFactory;
import com.example.cvcreator.ai.dto.SectionRequest;
import com.example.cvcreator.ai.dto.SectionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiCvService - Testy jednostkowe")
class AiCvServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private PromptFactory promptFactory;

    @InjectMocks
    private AiCvService aiCvService;

    // ==================== TESTY generateSection ====================

    @Test
    @DisplayName("generateSection - zwraca domyślną odpowiedź gdy userMessage jest null")
    void generateSection_shouldReturnDefaultResponse_whenUserMessageIsNull() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage(null)
                .currentText("Istniejący tekst")
                .build();

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent()).isEqualTo("Istniejący tekst");
        assertThat(response.getMessage()).isEqualTo("Hej! Napisz coś, żebym mógł Ci pomóc 😊");
        verify(geminiClient, never()).generate(anyString());
        verify(promptFactory, never()).buildSectionPrompt(any());
    }

    @Test
    @DisplayName("generateSection - zwraca domyślną odpowiedź gdy userMessage jest pusty")
    void generateSection_shouldReturnDefaultResponse_whenUserMessageIsEmpty() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("")
                .currentText("Tekst CV")
                .build();

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent()).isEqualTo("Tekst CV");
        assertThat(response.getMessage()).isEqualTo("Hej! Napisz coś, żebym mógł Ci pomóc 😊");
        verify(geminiClient, never()).generate(anyString());
    }

    @Test
    @DisplayName("generateSection - zwraca domyślną odpowiedź gdy userMessage zawiera same białe znaki")
    void generateSection_shouldReturnDefaultResponse_whenUserMessageIsWhitespace() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("EXPERIENCE")
                .userMessage("   \n\t  ")
                .currentText("Obecny tekst")
                .build();

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent()).isEqualTo("Obecny tekst");
        assertThat(response.getMessage()).isEqualTo("Hej! Napisz coś, żebym mógł Ci pomóc 😊");
        verify(geminiClient, never()).generate(anyString());
    }

    @Test
    @DisplayName("generateSection - wywołuje GeminiClient gdy userMessage jest poprawny")
    void generateSection_shouldCallGeminiClient_whenUserMessageIsValid() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("EXPERIENCE")
                .userMessage("Dodaj doświadczenie w Java")
                .currentText("Moje CV")
                .build();

        String expectedPrompt = "generated-prompt";
        String aiResponse = "[CONTENT]Nowy tekst CV[/CONTENT][MESSAGE]Dodałem doświadczenie[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(request)).thenReturn(expectedPrompt);
        when(geminiClient.generate(expectedPrompt)).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        verify(promptFactory).buildSectionPrompt(request);
        verify(geminiClient).generate(expectedPrompt);
        assertThat(response.getContent()).isEqualTo("Nowy tekst CV");
        assertThat(response.getMessage()).isEqualTo("Dodałem doświadczenie");
    }

    @Test
    @DisplayName("generateSection - zwraca pusty content gdy currentText jest null i userMessage pusty")
    void generateSection_shouldReturnEmptyContent_whenCurrentTextIsNullAndUserMessageEmpty() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SKILLS")
                .userMessage("")
                .currentText(null)
                .build();

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("Hej! Napisz coś, żebym mógł Ci pomóc 😊");
    }

    // ==================== TESTY parseResponse ====================

    @Test
    @DisplayName("parseResponse - poprawnie parsuje odpowiedź z CONTENT i MESSAGE")
    void parseResponse_shouldParseCorrectly_whenBothTagsPresent() {
        // Given
        String aiResponse = "[CONTENT]Tekst sekcji CV[/CONTENT][MESSAGE]Sekcja gotowa![/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("SUMMARY")
                        .userMessage("test")
                        .currentText("Stary tekst")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Tekst sekcji CV");
        assertThat(response.getMessage()).isEqualTo("Sekcja gotowa!");
    }

    @Test
    @DisplayName("parseResponse - zwraca currentText gdy content to '(pusta - tworzysz od zera)'")
    void parseResponse_shouldReturnCurrentText_whenContentIsEmpty() {
        // Given
        String aiResponse = "[CONTENT](pusta - tworzysz od zera)[/CONTENT][MESSAGE]Zaczynamy od zera[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("EDUCATION")
                        .userMessage("Stwórz nową sekcję")
                        .currentText("Zachowany tekst")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Zachowany tekst");
        assertThat(response.getMessage()).isEqualTo("Zaczynamy od zera");
    }

    @Test
    @DisplayName("parseResponse - zwraca domyślny message gdy brakuje MESSAGE")
    void parseResponse_shouldReturnDefaultMessage_whenMessageTagMissing() {
        // Given
        String aiResponse = "[CONTENT]Nowa treść CV[/CONTENT]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("SKILLS")
                        .userMessage("Zmień CV")
                        .currentText("Stary")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Nowa treść CV");
        assertThat(response.getMessage()).isEqualTo("Stworzyłem propozycję. Sprawdź czy jest OK!");
    }

    @Test
    @DisplayName("parseResponse - usuwa bloki kodu z odpowiedzi")
    void parseResponse_shouldRemoveCodeBlocks_fromContent() {
        // Given
        // Używamy bloku kodu bez nowych linii, ponieważ Java regex . nie matchuje \n bez flagi DOTALL
        String aiResponse = "[CONTENT]Przed ```code here``` i po ```more code```[/CONTENT][MESSAGE]OK[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("PROJECTS")
                        .userMessage("test")
                        .currentText("")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Przed  i po");
        assertThat(response.getContent()).doesNotContain("```");
        assertThat(response.getContent()).doesNotContain("code");
    }

    @Test
    @DisplayName("parseResponse - normalizuje nadmiarowe gwiazdki")
    void parseResponse_shouldNormalizeAsterisks_inContent() {
        // Given
        String aiResponse = "[CONTENT]***Bold*** text **normal** ***more***[/CONTENT][MESSAGE]Done[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("SUMMARY")
                        .userMessage("test")
                        .currentText("")
                        .build()
        );

        // Then
        assertThat(response.getContent()).doesNotContain("***");
        assertThat(response.getContent()).contains("**");
    }

    // ==================== TESTY extractBetween ====================

    @Test
    @DisplayName("extractBetween - poprawnie wyciąga tekst między znacznikami")
    void extractBetween_shouldExtractCorrectly_whenBothTagsPresent() {
        // Given
        String aiResponse = "[CONTENT]Wyciągnij mnie[/CONTENT]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("SUMMARY")
                        .userMessage("test")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Wyciągnij mnie");
    }

    @Test
    @DisplayName("extractBetween - zwraca pusty string gdy brakuje znacznika startowego")
    void extractBetween_shouldReturnEmpty_whenStartTagMissing() {
        // Given
        String aiResponse = "Brak tagu startowego[/CONTENT][MESSAGE]Test[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("EXPERIENCE")
                        .userMessage("test")
                        .currentText("Fallback")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Fallback");
    }

    @Test
    @DisplayName("extractBetween - zwraca pusty string gdy brakuje znacznika końcowego")
    void extractBetween_shouldReturnEmpty_whenEndTagMissing() {
        // Given
        String aiResponse = "[CONTENT]Tekst bez zamknięcia[MESSAGE]OK[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("SKILLS")
                        .userMessage("test")
                        .currentText("Domyślny")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Domyślny");
    }

    @Test
    @DisplayName("extractBetween - bierze pierwsze wystąpienie przy wielu znacznikach")
    void extractBetween_shouldTakeFirst_whenMultipleTags() {
        // Given
        String aiResponse = "[CONTENT]Pierwszy[/CONTENT] [CONTENT]Drugi[/CONTENT][MESSAGE]Msg[/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("EDUCATION")
                        .userMessage("test")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Pierwszy");
    }

    @Test
    @DisplayName("extractBetween - trim'uje białe znaki")
    void extractBetween_shouldTrimWhitespace() {
        // Given
        String aiResponse = "[CONTENT]  \n  Tekst z białymi znakami  \n  [/CONTENT][MESSAGE]  Wiadomość  [/MESSAGE]";

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(
                SectionRequest.builder()
                        .sectionType("PROJECTS")
                        .userMessage("test")
                        .build()
        );

        // Then
        assertThat(response.getContent()).isEqualTo("Tekst z białymi znakami");
        assertThat(response.getMessage()).isEqualTo("Wiadomość");
    }

    // ==================== TESTY INTEGRACYJNE Z MOCKAMI ====================

    @Test
    @DisplayName("Integracja - buildSectionPrompt jest wywoływane z poprawnym requestem")
    void integration_shouldCallBuildSectionPrompt_withCorrectRequest() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .userMessage("Dodaj skill Java")
                .currentText("CV content")
                .sectionType("SKILLS")
                .build();

        when(promptFactory.buildSectionPrompt(request)).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn("[CONTENT]Skills[/CONTENT][MESSAGE]OK[/MESSAGE]");

        // When
        aiCvService.generateSection(request);

        // Then
        verify(promptFactory).buildSectionPrompt(request);
    }

    @Test
    @DisplayName("Integracja - generate jest wywoływane z promptem z PromptFactory")
    void integration_shouldCallGenerate_withPromptFromFactory() {
        // Given
        String expectedPrompt = "Wygenerowany prompt dla AI";

        when(promptFactory.buildSectionPrompt(any())).thenReturn(expectedPrompt);
        when(geminiClient.generate(expectedPrompt)).thenReturn("[CONTENT]Result[/CONTENT][MESSAGE]Done[/MESSAGE]");

        // When
        aiCvService.generateSection(
                SectionRequest.builder()
                        .userMessage("Test")
                        .build()
        );

        // Then
        verify(geminiClient).generate(expectedPrompt);
    }

    @Test
    @DisplayName("Integracja - kompletny przepływ od requesta do response")
    void integration_shouldCompleteFullFlow_fromRequestToResponse() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SUMMARY")
                .userMessage("Napisz summary")
                .currentText("Stare summary")
                .build();

        String prompt = "System prompt dla summary";
        String aiResponse = "[CONTENT]**Nowe** profesjonalne summary ```json{}```[/CONTENT][MESSAGE]Summary zaktualizowane![/MESSAGE]";

        when(promptFactory.buildSectionPrompt(request)).thenReturn(prompt);
        when(geminiClient.generate(prompt)).thenReturn(aiResponse);

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        verify(promptFactory).buildSectionPrompt(request);
        verify(geminiClient).generate(prompt);

        assertThat(response.getContent())
                .isEqualTo("**Nowe** profesjonalne summary")
                .doesNotContain("```");
        assertThat(response.getMessage()).isEqualTo("Summary zaktualizowane!");
    }

    @Test
    @DisplayName("Integracja - obsługa odpowiedzi AI bez znaczników")
    void integration_shouldHandleResponse_withoutTags() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("EXPERIENCE")
                .userMessage("Update CV")
                .currentText("Original content")
                .build();

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn("Odpowiedź bez tagów");

        // When
        SectionResponse response = aiCvService.generateSection(request);

        // Then
        assertThat(response.getContent()).isEqualTo("Original content");
        assertThat(response.getMessage()).isEqualTo("Stworzyłem propozycję. Sprawdź czy jest OK!");
    }

    @Test
    @DisplayName("Integracja - weryfikacja że metody są wywoływane dokładnie raz")
    void integration_shouldCallMethodsExactlyOnce_inHappyPath() {
        // Given
        SectionRequest request = SectionRequest.builder()
                .sectionType("SKILLS")
                .userMessage("Valid message")
                .build();

        when(promptFactory.buildSectionPrompt(any())).thenReturn("prompt");
        when(geminiClient.generate(anyString())).thenReturn("[CONTENT]Text[/CONTENT][MESSAGE]Msg[/MESSAGE]");

        // When
        aiCvService.generateSection(request);

        // Then
        verify(promptFactory, times(1)).buildSectionPrompt(any());
        verify(geminiClient, times(1)).generate(anyString());
    }
}