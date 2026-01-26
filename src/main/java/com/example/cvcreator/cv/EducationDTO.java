package com.example.cvcreator.cv;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class EducationDTO {
    private UUID id;
    private String degree;
    private String institution;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}