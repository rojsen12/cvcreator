package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.DocumentEditRequest;
import com.example.cvcreator.ai.dto.DocumentEditResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEditService {

    private final GeminiClient geminiClient;

    public DocumentEditResponse editDocument(DocumentEditRequest request) {
        boolean isFragmentEdit = isFragmentEditRequest(request.getUserPrompt());

        String prompt = isFragmentEdit
                ? buildFragmentEditPrompt(request)
                : buildFullDocumentEditPrompt(request);

        String aiResponse = geminiClient.generate(prompt);

        return parseEditResponse(aiResponse, request);
    }

    private boolean isFragmentEditRequest(String userPrompt) {
        String lower = userPrompt.toLowerCase();
        return lower.contains("[formatowanie]")
                || lower.contains("kolor")
                || lower.contains("czcionk")
                || lower.contains("font")
                || lower.contains("zmień na")
                || lower.contains("edytuj tylko ten fragment")
                || lower.startsWith("[formatowanie");
    }

    private String buildFragmentEditPrompt(DocumentEditRequest req) {
        String userPrompt = req.getUserPrompt();

        if (detectContentChange(userPrompt)) {
            return String.format("""
                [DOCUMENT]
                %s
                [/DOCUMENT]
                
                [MESSAGE]
                ❌ Nie mogę zmienić treści tekstu. W tym trybie możesz tylko formatować:
                • Pogrubienie, kursywa, podkreślenie
                • Kolor tekstu (np. czerwony, niebieski)
                • Wielkość czcionki (np. 14pt, powiększ)
                • Rodzaj czcionki (np. Arial, Roboto, Montserrat, Courier New, Times New Roman)
                • Wyrównanie (wyśrodkuj)
                
                🔙 Aby zmienić treść, kliknij "← Wróć do edycji"
                [/MESSAGE]
                """,
                    req.getCurrentDocument()
            );
        }

        return String.format("""
            Tekst do sformatowania: %s
            Polecenie użytkownika: %s
            
            TWOJE ZADANIE:
            Owiń podany tekst w odpowiednie tagi HTML. 
            !!! ZACHOWAJ DOKŁADNIE TĘ SAMĄ TREŚĆ TEKSTU !!! Nie zmieniaj ani jednego słowa.
            
            DOZWOLONE TAGI I STYLE:
            - <b>tekst</b> - pogrubienie
            - <i>tekst</i> - kursywa  
            - <u>tekst</u> - podkreślenie
            - <span style="color: [KOLOR]">...</span> - kolor (np. red, blue, green, #HEX)
            - <span style="font-family: [NAZWA]">...</span> - ZMIANA CZCIONKI (np. Arial, 'Times New Roman', Courier, Roboto, Montserrat, Georgia, Verdana)
            - <span style="font-size: [PKT]pt">...</span> - wielkość (np. 14pt, 10pt)
            - <div style="text-align: center/right/left">...</div> - wyrównanie
            
            Możesz łączyć style, np: <span style="font-family: Arial; color: blue; font-size: 14pt">tekst</span>
            
            FORMAT ODPOWIEDZI:
            [DOCUMENT]
            tutaj sformatowany HTML
            [/DOCUMENT]
            
            [MESSAGE]
            Krótki opis co zrobiłeś (np. "Zmieniłem czcionkę na Arial i pogrubiłem tekst")
            [/MESSAGE]
            """,
                req.getCurrentDocument(),
                userPrompt
        );
    }

    private boolean detectContentChange(String userPrompt) {
        String lower = userPrompt.toLowerCase();

        if (lower.contains("czcionk") || lower.contains("font") || lower.contains("kolor") ||
                lower.contains("pogrub") || lower.contains("rozmiar") || lower.contains("kursyw")) {
            return false;
        }

        String[] contentChangeKeywords = {
                "zamień na", "przepisz", "napisz", "dodaj", "usuń", "wstaw",
                "zmień tekst", "popraw", "skoryguj", "zastąp", "przeformułuj"
        };

        for (String keyword : contentChangeKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildFullDocumentEditPrompt(DocumentEditRequest req) {
        return String.format("""
            Użytkownik chce edytować dokument: "%s"
            
            TWOJE ZADANIE:
            Poinformuj użytkownika, że w tym trybie można tylko formatować ZAZNACZONY fragment.
            
            FORMAT ODPOWIEDZI:
            [DOCUMENT]
            %s
            [/DOCUMENT]
            
            [MESSAGE]
            ℹ️ Aby sformatować tekst:
            1. Zaznacz fragment tekstu myszką.
            2. Wybierz czcionkę z listy lub wpisz komendę (np. "pogrub", "zmień na czerwony").
            [/MESSAGE]
            """,
                req.getUserPrompt(),
                req.getCurrentDocument()
        );
    }

    private DocumentEditResponse parseEditResponse(String text, DocumentEditRequest request) {
        String document = extractBetween(text, "[DOCUMENT]", "[/DOCUMENT]");
        String message = extractBetween(text, "[MESSAGE]", "[/MESSAGE]");

        if (document.isEmpty()) {
            if (text.contains("<") && text.contains(">")) {
                document = text.replaceAll("```html\\s*", "").replaceAll("```\\s*", "").trim();
                int firstTag = document.indexOf("<");
                int lastTag = document.lastIndexOf(">") + 1;
                if (firstTag >= 0 && lastTag > firstTag) {
                    document = document.substring(firstTag, lastTag);
                }
            }
        }

        if (document.isEmpty()) {
            document = request.getCurrentDocument();
            if (message.isEmpty()) message = extractMessageFromPlainText(text);
        }

        if (message.isEmpty()) message = "Zaktualizowano formatowanie.";

        return new DocumentEditResponse(document, message);
    }

    private String extractMessageFromPlainText(String text) {
        String cleaned = text.replaceAll("\\[/?DOCUMENT\\]", "")
                .replaceAll("\\[/?MESSAGE\\]", "")
                .replaceAll("<[^>]+>", "")
                .replaceAll("```[^`]*```", "")
                .trim();
        return cleaned.length() > 500 ? cleaned.substring(0, 500) : cleaned;
    }

    private String extractBetween(String text, String start, String end) {
        try {
            int startIdx = text.indexOf(start);
            int endIdx = text.indexOf(end);
            if (startIdx == -1 || endIdx == -1) return "";
            return text.substring(startIdx + start.length(), endIdx).trim();
        } catch (Exception e) {
            return "";
        }
    }
}