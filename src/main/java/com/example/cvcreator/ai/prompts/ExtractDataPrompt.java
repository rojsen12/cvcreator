package com.example.cvcreator.ai.prompts;

public class ExtractDataPrompt {

    public static String build(String userInput) {
        return """
Wyciągnij SUROWE dane z tekstu użytkownika. NIE GENERUJ treści, tylko PRZEPISZ fakty.

ZASADY KRYTYCZNE:
- Nie dodawaj NICZEGO od siebie
- Nie poprawiaj, nie interpretuj
- Jeśli czegoś nie ma → null lub []
- Przepisuj dane 1:1

ZWRÓĆ TYLKO CZYSTY JSON (bez markdown, bez komentarzy):

{
  "personalInfo": {
    "firstName": null,
    "lastName": null,
    "email": null,
    "phone": null,
    "address": null,
    "city": null,
    "country": null,
    "linkedin": null,
    "github": null
  },
  "rawExperience": [],
  "rawEducation": [],
  "rawSkills": [],
  "rawProjects": [],
  "rawLanguages": [],
  "rawInterests": []
}

INSTRUKCJA:
- personalInfo: wszystkie dane kontaktowe (imię, nazwisko, email, telefon, adres, LinkedIn, GitHub)
- rawExperience: lista stringów, każdy wpis to jedno doświadczenie zawodowe
- rawEducation: lista stringów (uczelnia, kierunek, stopień, lata)
- rawSkills: lista umiejętności technicznych i miękkich
- rawProjects: lista projektów z opisami
- rawLanguages: lista języków obcych z poziomami (np. "Angielski B2")
- rawInterests: zainteresowania

DANE UŻYTKOWNIKA:
%s

Zwróć TYLKO JSON.
""".formatted(userInput);
    }
}