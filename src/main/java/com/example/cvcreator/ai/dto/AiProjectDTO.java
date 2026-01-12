package com.example.cvcreator.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AiProjectDTO {
    private UUID id;
    private String name;
    private String description;
    private List<String> technologies;
    private String link;
}