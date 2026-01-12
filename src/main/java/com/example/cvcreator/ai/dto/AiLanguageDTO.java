package com.example.cvcreator.ai.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AiLanguageDTO {
    private UUID id;
    private String name;
    private String level;
}