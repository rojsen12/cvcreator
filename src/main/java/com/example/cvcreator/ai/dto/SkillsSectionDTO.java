package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillsSectionDTO {
    private AiSkillsDTO skills = new AiSkillsDTO();
}