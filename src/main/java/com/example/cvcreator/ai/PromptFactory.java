package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.SectionRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptFactory {

    private static final Map<String, String> SECTION_RULES = Map.of(
            "personalInfo", """
            ZASADY: Imię i Nazwisko, a pod spodem dane kontaktowe: Email, Telefon, Lokalizacja, LinkedIn. 
            STYL: Każda informacja w nowej linii, bez zbędnych opisów.""",

            "summary", """
            ZASADY: Tekst ciągły w formie akapitu. 
            STYL: Narracja zawodowa. Jeśli użytkownik prosi o 'rozwinięcie', stwórz przekonujący opis celów i sukcesów zawodowych.""",

            "experience", """
            ZASADY: Każde stanowisko jako osobny blok. 
            UKŁAD: Stanowisko - Firma | Okres. Poniżej lista obowiązków od myślników.
            STYL: Używaj czasowników dokonanych (np. 'wdrożyłem', 'zoptymalizowałem'). Jeśli użytkownik użyje 'rozwiń',
            zamień proste zadania na profesjonalne opisy osiągnięć.""",

            "education", """
            ZASADY: Kierunek studiów - Uczelnia | Rok ukończenia.
            STYL: Formalny i przejrzysty.""",

            "skills", """
            ZASADY: Podział na logiczne kategorie (np. Języki programowania, Umiejętności miękkie).
            STYL: Wypunktowanie lub lista po przecinku wewnątrz kategorii.""",

            "projects", """
            ZASADY: Nazwa projektu, Rola, Opis (Rozwiązanie i Efekty).
            STYL: Jeśli użytkownik prosi o 'poprawę' lub 'rozwinięcie', sformatuj to w sposób opisowy, używając pełnych zdań tam, gdzie tłumaczone są cele projektu.""",

            "interests", """
            ZASADY: Krótka lista zainteresowań.
            STYL: Naturalny, unikaj przesadnie formalnego tonu."""
    );

    public String buildSectionPrompt(SectionRequest req) {
        String rule = SECTION_RULES.getOrDefault(req.getSectionType(), "Zachowaj strukturę i profesjonalny ton.");

        return String.format("""
            === TWOJA ROLA ===
            Jesteś doświadczonym konsultantem kariery i ekspertem od pisania profesjonalnych CV. 
            Twoim celem jest przygotowanie treści sekcji: %s.

            === KLUCZOWE LOGIKA DZIAŁANIA ===
            1. SŁOWA KLUCZE: Jeśli użytkownik pisze "rozwiń", "opis", "profesjonalnie" – weź jego krótkie hasła i zamień je w bogate, merytoryczne opisy pasujące do standardów rynkowych.
            2. NIE NADPISUJ: Jeśli w polu "Obecna treść" są już dane, a użytkownik dodaje coś nowego (np. "dodałem kurs", "pracowałem też w"), MUSISZ zachować stare informacje i dopisać nową pozycję.
            3. KOMUNIKATYWNOŚĆ: Rozmawiaj z użytkownikiem w sekcji [MESSAGE]. Bądź pomocny, doradzaj poprawki, zachowuj się jak człowiek, a nie automat.

            === ZASADY FORMATOWANIA (KRYTYCZNE) ===
            1. ZERO GWIAZDEK: Absolutny zakaz używania znaków * oraz **. Nie używaj ich do pogrubień ani list.
            2. NAGŁÓWKI: Nie pisz całych nagłówków Caps Lockiem (np. zamiast "DOŚWIADCZENIE" napisz "Doświadczenie zawodowe").
            3. MYŚLNIKI: Do list używaj standardowego myślnika (-).
            4. PEŁNE ZDANIA: Tam, gdzie to możliwe i naturalne (np. podsumowanie, opisy projektów, osiągnięcia), buduj poprawne, pełne zdania.

            === KONTEKST ===
            Sekcja: %s
            Wytyczne stylu: %s
            Język odpowiedzi: %s

            === DANE ===
            Obecna treść w CV: 
            %s

            Polecenie użytkownika: 
            %s

            === STRUKTURA ODPOWIEDZI ===
            Pamiętaj: [CONTENT] musi zawierać TYLKO czysty tekst do CV (całą sekcję od nowa z uwzględnieniem zmian).
            
            [CONTENT]
            (Wygeneruj zaktualizowaną treść sekcji - bez gwiazdek, z naturalnymi nagłówkami)
            [/CONTENT]

            [MESSAGE]
            (Napisz krótką, miłą wiadomość do użytkownika. Powiedz co zmieniłeś/rozwinąłeś i czy masz jakieś sugestie.)
            [/MESSAGE]
            """,
                req.getSectionType(),
                req.getSectionType(),
                rule,
                req.getLanguage(),
                (req.getCurrentText() != null && !req.getCurrentText().isEmpty()) ? req.getCurrentText() : "Brak danych.",
                req.getUserMessage()
        );
    }
}