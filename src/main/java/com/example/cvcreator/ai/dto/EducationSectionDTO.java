package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EducationSectionDTO {
    private List<AiEducationDTO> education = new ArrayList<>();
}