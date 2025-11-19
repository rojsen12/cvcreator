package com.example.cvcreator.cv;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ExperienceDTO {
    private UUID id;
    private String position;
    private String company;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}