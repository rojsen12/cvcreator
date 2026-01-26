package com.example.cvcreator.ai;

import com.example.cvcreator.ai.dto.DocumentEditRequest;
import com.example.cvcreator.ai.dto.DocumentEditResponse;
import com.example.cvcreator.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cv-edit")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class DocumentEditController {

    private final DocumentEditService documentEditService;

    @PostMapping("/edit-document")
    public ResponseEntity<DocumentEditResponse> editDocument(
            @RequestBody DocumentEditRequest request,
            Authentication authentication
    ) {
        try {
            DocumentEditResponse response = documentEditService.editDocument(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(new DocumentEditResponse(
                    request.getCurrentDocument(),
                    "Przepraszam, nie udało się przetworzyć Twojej prośby. Spróbuj inaczej."
            ));
        }
    }
}