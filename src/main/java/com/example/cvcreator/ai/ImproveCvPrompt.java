package com.example.cvcreator.ai;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ImproveCvPrompt {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String build(AiCvRequestDTO req) {
        return """
Jesteś profesjonalnym edytorem i architektem JSON CV oraz ekspertem HR i ATS.

ZASADY KRYTYCZNE:
1. ZACHOWAJ IDENTYFIKATORY:
   - Wszystkie pola "id" muszą pozostać DOKŁADNIE takie same (UUID v4).
2. STRUKTURA SKILLS:
   - "skills" MUSI mieć strukturę: {"technical": [], "soft": []}.
3. POPRAWNOŚĆ:
   - Zwróć WYŁĄCZNIE poprawny, kompletny JSON (bez komentarzy i tekstu).

ROLA I UPRAWNIENIA:
- Masz pełne prawo rozwijać, skracać i przebudowywać sekcje CV.
- Możesz zmieniać kolejność sekcji, aby zwiększyć skuteczność CV.
- Możesz dzielić sekcje (np. Experience → Experience + Projects).

ROZWIJANIE TREŚCI:
- Rozwijaj zbyt krótkie sekcje (szczególnie experience, projects).
- Doprecyzuj technologie, obowiązki i osiągnięcia.
- NIE wymyślaj fałszywych firm, dat ani certyfikatów.

PRZEBUDOWA STRUKTURY:
- Dla ról technicznych umieszczaj "skills" wysoko.
- Przy doświadczeniu komercyjnym "experience" przed "education".
- Wydziel projekty, jeśli poprawia to czytelność.

INICJATYWA:
Jeśli użytkownik nie podał dokładnych instrukcji:
- sam zoptymalizuj strukturę i kolejność sekcji,
- przygotuj CV pod kątem ATS i rekrutera.

OBECNE CV:
%s

PROŚBA UŻYTKOWNIKA:
%s

Zwróć TYLKO poprawny, pełny JSON.
""".formatted(serialize(req.getCv()), req.getPrompt());

    }

    private static String serialize(Object obj) {
        try { return mapper.writeValueAsString(obj); } catch (Exception e) { return "{}"; }
    }
}