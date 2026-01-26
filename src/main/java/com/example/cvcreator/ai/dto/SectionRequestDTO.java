package com.example.cvcreator.ai.dto;

import lombok.Data;

@Data
public class SectionRequestDTO {
    private String sectionType;
    private String userInput;
    private String language;
    private Object existingData;
}