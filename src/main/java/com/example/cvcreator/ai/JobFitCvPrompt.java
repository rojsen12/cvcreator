package com.example.cvcreator.ai;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JobFitCvPrompt {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String build(AiCvRequestDTO req) {
        return """
        Jesteś specjalistą ATS. Dopasuj istniejące CV do oferty pracy poprzez zmianę akcentów i słów kluczowych.

        ZASADY:
        1. NIE dopisuj doświadczeń, których nie ma w DANYCH WEJŚCIOWYCH.
        2. Zmień "summary" i kolejność "skills", aby lepiej odpowiadały OFERCIE PRACY.
        3. Jeśli oferta wymaga technologii, którą użytkownik ma w CV, przesuń ją na początek listy.
        4. Zachowaj wszystkie "id".

        OFERTA PRACY:
        %s

        OBECNE CV (JSON):
        %s

        JĘZYK: %s
        """.formatted(
                req.getJobDescription(),
                serialize(req.getCv()),
                req.getLanguage()
        );
    }

    private static String serialize(Object obj) {
        try { return mapper.writeValueAsString(obj); } catch (Exception e) { return "{}"; }
    }
}