package com.example.cvcreator.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SectionResult {

    // Experience
    private List<Experience> experience;

    // Education
    private List<Education> education;

    // Skills
    private Skills skills;

    // Projects
    private List<Project> projects;

    // Summary
    private String summary;

    // Getters & Setters
    public List<Experience> getExperience() { return experience; }
    public void setExperience(List<Experience> experience) { this.experience = experience; }

    public List<Education> getEducation() { return education; }
    public void setEducation(List<Education> education) { this.education = education; }

    public Skills getSkills() { return skills; }
    public void setSkills(Skills skills) { this.skills = skills; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    // Inner classes
    public static class Experience {
        private String title;
        private String company;
        private String period;
        private List<String> description;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public List<String> getDescription() { return description; }
        public void setDescription(List<String> description) { this.description = description; }
    }

    public static class Education {
        private String degree;
        private String field;
        private String institution;
        private String period;

        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
    }

    public static class Skills {
        private List<String> technical;
        private List<String> soft;
        private List<String> languages;

        public List<String> getTechnical() { return technical; }
        public void setTechnical(List<String> technical) { this.technical = technical; }

        public List<String> getSoft() { return soft; }
        public void setSoft(List<String> soft) { this.soft = soft; }

        public List<String> getLanguages() { return languages; }
        public void setLanguages(List<String> languages) { this.languages = languages; }
    }

    @Setter
    @Getter
    public static class Project {
        private String name;
        private String description;
        private List<String> technologies;

    }
}