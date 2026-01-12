package com.example.cvcreator.ai.prompts;

import com.example.cvcreator.ai.dto.ExtractedDataDTO;
import java.util.List;

public class SectionPrompts {

    public static String buildExperience(List<String> rawExperience, String language) {
        if (rawExperience == null || rawExperience.isEmpty()) {
            return null;
        }

        return """
Stwórz sekcję EXPERIENCE używając TYLKO tych danych:
%s

KRYTYCZNE ZASADY:
1. Użyj TYLKO firm, stanowisk i dat z danych powyżej
2. Jeśli brak informacji → ustaw null
3. Rozwiń description do 3-4 punktów (rozwijaj STYL, nie fakty)
4. NIE DODAWAJ technologii, narzędzi, metodyk, których użytkownik NIE wymienił

JĘZYK WYJŚCIA: %s

Zwróć TYLKO czysty JSON:
{
  "experience": [
    {
      "title": "...",
      "company": null lub "...",
      "location": null lub "...",
      "startDate": null lub "...",
      "endDate": null lub "...",
      "description": ["...", "...", "..."]
    }
  ]
}
""".formatted(String.join("\n", rawExperience), language);
    }

    public static String buildEducation(List<String> rawEducation, String language) {
        if (rawEducation == null || rawEducation.isEmpty()) {
            return null;
        }

        return """
Stwórz sekcję EDUCATION z danych:
%s

ZASADY:
- Użyj TYLKO uczelni, kierunków i dat z danych
- NIE dodawaj informacji, których nie podano
- degree: stopień (np. "Magister", "Inżynier", "Licencjat")
- fieldOfStudy: kierunek studiów
- university: nazwa uczelni
- location: lokalizacja (jeśli podano)

JĘZYK WYJŚCIA: %s

Zwróć TYLKO JSON:
{
  "education": [
    {
      "degree": null lub "...",
      "fieldOfStudy": "...",
      "university": "...",
      "location": null lub "...",
      "startDate": null lub "...",
      "endDate": null lub "...",
      "description": null lub ["..."]
    }
  ]
}
""".formatted(String.join("\n", rawEducation), language);
    }

    public static String buildSkills(List<String> rawSkills, String language) {
        if (rawSkills == null || rawSkills.isEmpty()) {
            return null;
        }

        return """
Kategoryzuj TYLKO te umiejętności:
%s

ZASADY:
1. Podziel na: technical (techniczne) i soft (miękkie)
2. NIE DODAWAJ żadnych nowych umiejętności
3. Możesz poprawić pisownię (np. "java" → "Java")
4. Technical: języki programowania, frameworki, narzędzia, bazy danych
5. Soft: komunikacja, zarządzanie, praca zespołowa, etc.

JĘZYK WYJŚCIA: %s

Zwróć TYLKO JSON:
{
  "skills": {
    "technical": [],
    "soft": []
  }
}
""".formatted(String.join(", ", rawSkills), language);
    }

    public static String buildLanguages(List<String> rawLanguages, String language) {
        if (rawLanguages == null || rawLanguages.isEmpty()) {
            return null;
        }

        return """
Stwórz listę języków obcych z danych:
%s

ZASADY:
- name: nazwa języka
- level: poziom (A1, A2, B1, B2, C1, C2, Native, Fluent, etc.)
- Jeśli poziom nie podany → null

JĘZYK WYJŚCIA: %s

Zwróć TYLKO JSON:
{
  "languages": [
    {
      "name": "...",
      "level": null lub "..."
    }
  ]
}
""".formatted(String.join(", ", rawLanguages), language);
    }

    public static String buildProjects(List<String> rawProjects, String language) {
        if (rawProjects == null || rawProjects.isEmpty()) {
            return null;
        }

        return """
Stwórz sekcję PROJECTS z danych:
%s

ZASADY:
- Użyj TYLKO projektów podanych powyżej
- name: nazwa projektu
- description: krótki opis (1-2 zdania)
- technologies: lista technologii TYLKO jeśli wymieniono
- link: link do projektu TYLKO jeśli podano
- NIE dodawaj technologii, których użytkownik nie wymienił

JĘZYK WYJŚCIA: %s

Zwróć TYLKO JSON:
{
  "projects": [
    {
      "name": "...",
      "description": "...",
      "technologies": null lub [...],
      "link": null lub "..."
    }
  ]
}
""".formatted(String.join("\n", rawProjects), language);
    }

    public static String buildSummary(ExtractedDataDTO data, String language) {
        return """
Stwórz krótkie SUMMARY (2-3 zdania) na podstawie TYLKO tych danych:

Doświadczenie: %s
Umiejętności: %s
Wykształcenie: %s

ZASADY:
- 2-3 zdania
- Ogólny profil zawodowy
- NIE wymyślaj stanowisk ani technologii spoza danych
- Język: %s

Zwróć TYLKO JSON:
{
  "summary": "..."
}
""".formatted(
                data.getRawExperience(),
                data.getRawSkills(),
                data.getRawEducation(),
                language
        );
    }
}