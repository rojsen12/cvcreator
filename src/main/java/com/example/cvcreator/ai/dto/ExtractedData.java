package com.example.cvcreator.ai.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractedData {

    @JsonProperty("personalInfo")
    private PersonalInfo personalInfo;

    @JsonProperty("rawExperience")
    private List<String> rawExperience;

    @JsonProperty("rawEducation")
    private List<String> rawEducation;

    @JsonProperty("rawSkills")
    private List<String> rawSkills;

    @JsonProperty("rawProjects")
    private List<String> rawProjects;

    @JsonProperty("rawInterests")
    private List<String> rawInterests;

    // Getters & Setters
    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }

    public List<String> getRawExperience() { return rawExperience; }
    public void setRawExperience(List<String> rawExperience) { this.rawExperience = rawExperience; }

    public List<String> getRawEducation() { return rawEducation; }
    public void setRawEducation(List<String> rawEducation) { this.rawEducation = rawEducation; }

    public List<String> getRawSkills() { return rawSkills; }
    public void setRawSkills(List<String> rawSkills) { this.rawSkills = rawSkills; }

    public List<String> getRawProjects() { return rawProjects; }
    public void setRawProjects(List<String> rawProjects) { this.rawProjects = rawProjects; }

    public List<String> getRawInterests() { return rawInterests; }
    public void setRawInterests(List<String> rawInterests) { this.rawInterests = rawInterests; }

    // Inner class
    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String location;

        // Getters & Setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}