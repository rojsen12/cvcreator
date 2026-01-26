package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.SectionRequest;
import com.example.cvcreator.ai.dto.SectionResponse;
import com.example.cvcreator.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv-ai")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class AiCvController {

    private final AiCvService aiCvService;
    private final AiCVRepository aiCVRepository;

    @PostMapping("/generate")
    public ResponseEntity<SectionResponse> generate(
            @RequestBody SectionRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        SectionResponse response = aiCvService.generateSection(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-ai-cv")
    public ResponseEntity<CVDocument> getMyAiCv(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return aiCVRepository.findByUserId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public ResponseEntity<CVDocument> saveCv(
            @RequestBody CVDocument request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        UUID userId = user.getId();

        CVDocument cv = aiCVRepository.findByUserId(userId)
                .orElse(new CVDocument());

        cv.setUserId(userId);
        cv.setPersonalInfo(request.getPersonalInfo());
        cv.setSummary(request.getSummary());
        cv.setExperience(request.getExperience());
        cv.setEducation(request.getEducation());
        cv.setSkills(request.getSkills());
        cv.setProjects(request.getProjects());
        cv.setInterests(request.getInterests());
        cv.setSectionOrder(request.getSectionOrder());
        cv.setProfilePhoto(request.getProfilePhoto());
        cv.setLastModified(LocalDateTime.now());

        CVDocument savedCV = aiCVRepository.save(cv);

        return ResponseEntity.ok(savedCV);
    }
}