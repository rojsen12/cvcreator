package com.example.cvcreator.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class CvData {
    private String personalInfo;
    private String summary;
    private String experience;
    private String education;
    private String skills;
    private String projects;
    private String interests;
    private List<String> sectionOrder;
    private String profilePhoto;
}