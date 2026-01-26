package com.example.cvcreator.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionRequest {
    private String sectionType;
    private String currentText;
    private String userMessage;
    private String language;
}