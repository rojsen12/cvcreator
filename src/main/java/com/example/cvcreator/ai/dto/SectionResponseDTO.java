package com.example.cvcreator.ai.dto;

import lombok.Data;

@Data
public class SectionResponseDTO {
    private String sectionType;
    private Object generatedContent;  // wygenerowana sekcja
    private String rawJson;           // surowy JSON do podglądu
    private boolean success;
    private String message;
}