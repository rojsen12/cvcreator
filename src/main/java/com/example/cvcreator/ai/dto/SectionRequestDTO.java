package com.example.cvcreator.ai.dto;

import lombok.Data;

@Data
public class SectionRequestDTO {
    private String sectionType;      // "personalInfo", "experience", "education", etc.
    private String userInput;        // co użytkownik podał
    private String language;
    private Object existingData;     // opcjonalnie - jeśli chce poprawić
}