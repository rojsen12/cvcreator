package com.example.cvcreator.cv;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CVDTO {
    private UUID id;
    private String templateType;

    // Personal Info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;
    private String summary;

    // Collections
    private List<ExperienceDTO> experiences = new ArrayList<>();
    private List<EducationDTO> educations = new ArrayList<>();
    private List<String> skills = new ArrayList<>();
    private List<LanguageDTO> languages = new ArrayList<>();

    // Metadata
    private LocalDate createdAt;
    private LocalDate updatedAt;
}