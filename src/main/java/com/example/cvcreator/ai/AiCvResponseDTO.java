package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.AiCVDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiCvResponseDTO {
    private AiCVDTO cv;
    private String model;
    private String promptVersion;
    private LocalDateTime generatedAt;
}
