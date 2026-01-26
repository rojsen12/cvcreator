package com.example.cvcreator.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEditRequest {
    private String currentDocument;
    private String userPrompt;
    private String language;
}