package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.AiCVDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiCvRequestDTO {
    private AiCVDTO cv;
    private AiCvMode mode;
    private String jobDescription;
    private String language;
    private String prompt;
    private List<String> sectionOrder;
}