package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiCvService {

    private static final Logger log = LoggerFactory.getLogger(AiCvService.class);

    private final CvGeneratorService cvGeneratorService;
    private final ObjectMapper mapper;
    private final String modelName;

    public AiCvService(
            CvGeneratorService cvGeneratorService,
            ObjectMapper mapper,
            @Value("${gemini.model}") String modelName
    ) {
        this.cvGeneratorService = cvGeneratorService;
        this.mapper = mapper;
        this.modelName = modelName;
    }

    public AiCvResponseDTO process(AiCvRequestDTO req) {

        AiCVDTO cv;

        // Sprawdź czy to IMPROVE (dodawanie do istniejącego CV)
        if (req.getMode() == AiCvMode.IMPROVE && req.getCv() != null) {
            log.info("=== Tryb IMPROVE - rozbudowuję istniejące CV ===");
            cv = cvGeneratorService.improveCv(req);
        } else {
            log.info("=== Tryb GENERATE - tworzę nowe CV ===");
            cv = cvGeneratorService.generateCv(req);
        }

        AiCvResponseDTO response = new AiCvResponseDTO();
        response.setCv(cv);
        response.setModel(modelName);
        response.setPromptVersion("v2-incremental");
        response.setGeneratedAt(LocalDateTime.now());

        return response;
    }
}