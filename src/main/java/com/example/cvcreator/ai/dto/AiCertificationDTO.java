package com.example.cvcreator.ai.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AiCertificationDTO {
    private UUID id;
    private String name;
    private String issuer;
    private String date;
    private String link;
}