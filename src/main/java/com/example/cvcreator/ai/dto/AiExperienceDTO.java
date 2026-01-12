package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.*;

@Data
public class AiExperienceDTO {
    private UUID id = UUID.randomUUID();
    @JsonAlias({"title", "position", "jobTitle"})
    private String title;
    @JsonAlias({"company", "companyName"})
    private String company;
    private String location;
    private String startDate;
    private String endDate;
    @JsonAlias({"description", "descriptionPoints", "responsibilities"})
    private List<String> description = new ArrayList<>();
}