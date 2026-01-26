package com.example.cvcreator.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AiEducationDTO {
    private UUID id;
    private String degree;
    private String fieldOfStudy;
    private String university;
    private String location;
    private String startDate;
    private String endDate;
    private List<String> description;
}