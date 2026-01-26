package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiCVDTO {
    private UUID id = UUID.randomUUID();

    @JsonAlias({"personal_info", "contact"})
    private AiPersonalInfoDTO personalInfo = new AiPersonalInfoDTO();

    private String summary;

    @JsonAlias({"work_experience", "jobs"})
    private List<AiExperienceDTO> experience = new ArrayList<>();

    private List<AiEducationDTO> education = new ArrayList<>();

    private AiSkillsDTO skills = new AiSkillsDTO();

    private List<AiProjectDTO> projects = new ArrayList<>();

    private List<String> interests = new ArrayList<>();

    private List<String> sectionOrder = Arrays.asList("summary", "experience", "education", "skills", "projects", "interests");
}