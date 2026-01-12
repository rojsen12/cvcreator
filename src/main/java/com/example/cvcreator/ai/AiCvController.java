package com.example.cvcreator.ai;

import com.example.cvcreator.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/cv")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class AiCvController {

    private final AiCvService aiCvService;

    @PostMapping
    public ResponseEntity<AiCvResponseDTO> process(
            @RequestBody AiCvRequestDTO request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        AiCvResponseDTO response = aiCvService.process(request);
        return ResponseEntity.ok(response);
    }


}


