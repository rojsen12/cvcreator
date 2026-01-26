package com.example.cvcreator.cv;

import com.example.cvcreator.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class CVController {

    private final CVService cvService;

    @GetMapping
    public ResponseEntity<List<CVDTO>> getAllCVs(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<CVDTO> cvs = cvService.getAllCVsForUser(user.getId());
        return ResponseEntity.ok(cvs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CVDTO> getCVById(@PathVariable UUID id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CVDTO cv = cvService.getCVById(id, user.getId());
        return ResponseEntity.ok(cv);
    }

    @PostMapping
    public ResponseEntity<CVDTO> createCV(@RequestBody CVDTO cvDTO, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CVDTO createdCV = cvService.createCV(cvDTO, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCV);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CVDTO> updateCV(@PathVariable UUID id, @RequestBody CVDTO cvDTO, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CVDTO updatedCV = cvService.updateCV(id, cvDTO, user.getId());
        return ResponseEntity.ok(updatedCV);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCV(@PathVariable UUID id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cvService.deleteCV(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}