package com.example.cvcreator.ai;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cv_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CVDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String personalInfo;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @Column(columnDefinition = "TEXT")
    private String interests;

    @ElementCollection
    @CollectionTable(
            name = "cv_document_section_order",
            joinColumns = @JoinColumn(name = "cv_document_id")
    )
    @Column(name = "section_name")
    private List<String> sectionOrder;

    @Column(columnDefinition = "TEXT")
    private String profilePhoto;

    private LocalDateTime lastModified;
}