package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractedDataDTO {

    private PersonalInfoRaw personalInfo = new PersonalInfoRaw();
    private List<String> rawExperience = new ArrayList<>();
    private List<String> rawEducation = new ArrayList<>();
    private List<String> rawSkills = new ArrayList<>();
    private List<String> rawProjects = new ArrayList<>();
    private List<String> rawLanguages = new ArrayList<>();
    private List<String> rawInterests = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonalInfoRaw {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String country;
        private String linkedin;
        private String github;
    }
}