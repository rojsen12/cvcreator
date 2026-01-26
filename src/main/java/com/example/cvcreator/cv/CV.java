package com.example.cvcreator.cv;

import com.example.cvcreator.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cvs")
@Getter
@Setter
public class CV {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Education> educations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "cv_skills", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Language> languages = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt = LocalDate.now();

    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setCv(this);
    }

    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setCv(null);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setCv(this);
    }

    public void removeEducation(Education education) {
        educations.remove(education);
        education.setCv(null);
    }

    public void addLanguage(Language language) {
        languages.add(language);
        language.setCv(this);
    }

    public void removeLanguage(Language language) {
        languages.remove(language);
        language.setCv(null);
    }
}