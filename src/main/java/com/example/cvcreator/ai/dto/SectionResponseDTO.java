package com.example.cvcreator.ai.dto;

import lombok.Data;

@Data
public class SectionResponseDTO {
    private String sectionType;
    private Object generatedContent;
    private String rawJson;
    private boolean success;
    private String message;
}