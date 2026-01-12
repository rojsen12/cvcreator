package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.SectionRequestDTO;
import com.example.cvcreator.ai.dto.SectionResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cv-chat")
@CrossOrigin(origins = "*")
public class CvChatController {

    private final CvChatService cvChatService;

    public CvChatController(CvChatService cvChatService) {
        this.cvChatService = cvChatService;
    }

    @PostMapping("/generate-section")
    public ResponseEntity<SectionResponseDTO> generateSection(@RequestBody SectionRequestDTO request) {
        SectionResponseDTO response = cvChatService.generateSection(request);
        return ResponseEntity.ok(response);
    }
}