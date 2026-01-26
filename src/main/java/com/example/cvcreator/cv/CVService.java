package com.example.cvcreator.cv;

import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class CVService {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;

    public List<CVDTO> getAllCVsForUser(UUID userId) {
        return cvRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CVDTO getCVById(UUID cvId, UUID userId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new RuntimeException("CV not found or access denied"));
        return convertToDTO(cv);
    }

    public CVDTO createCV(CVDTO cvDTO, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CV cv = new CV();
        cv.setUser(user);
        cv.setTemplateType(cvDTO.getTemplateType());
        cv.setFirstName(cvDTO.getFirstName());
        cv.setLastName(cvDTO.getLastName());
        cv.setEmail(cvDTO.getEmail());
        cv.setPhone(cvDTO.getPhone());
        cv.setAddress(cvDTO.getAddress());
        cv.setProfilePicture(cvDTO.getProfilePicture());
        cv.setSummary(cvDTO.getSummary());
        cv.setSkills(cvDTO.getSkills());
        cv.setCreatedAt(LocalDate.now());
        cv.setUpdatedAt(LocalDate.now());

        if (cvDTO.getExperiences() != null) {
            cvDTO.getExperiences().forEach(expDTO -> {
                Experience exp = new Experience();
                exp.setPosition(expDTO.getPosition());
                exp.setCompany(expDTO.getCompany());
                exp.setLocation(expDTO.getLocation());
                exp.setStartDate(expDTO.getStartDate());
                exp.setEndDate(expDTO.getEndDate());
                exp.setDescription(expDTO.getDescription());
                cv.addExperience(exp);
            });
        }

        if (cvDTO.getEducations() != null) {
            cvDTO.getEducations().forEach(eduDTO -> {
                Education edu = new Education();
                edu.setDegree(eduDTO.getDegree());
                edu.setInstitution(eduDTO.getInstitution());
                edu.setLocation(eduDTO.getLocation());
                edu.setStartDate(eduDTO.getStartDate());
                edu.setEndDate(eduDTO.getEndDate());
                edu.setDescription(eduDTO.getDescription());
                cv.addEducation(edu);
            });
        }

        if (cvDTO.getLanguages() != null) {
            cvDTO.getLanguages().forEach(langDTO -> {
                Language lang = new Language();
                lang.setName(langDTO.getName());
                lang.setLevel(langDTO.getLevel());
                cv.addLanguage(lang);
            });
        }

        CV savedCV = cvRepository.save(cv);
        return convertToDTO(savedCV);
    }

    public CVDTO updateCV(UUID cvId, CVDTO cvDTO, UUID userId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new RuntimeException("CV not found or access denied"));

        cv.setTemplateType(cvDTO.getTemplateType());
        cv.setFirstName(cvDTO.getFirstName());
        cv.setLastName(cvDTO.getLastName());
        cv.setEmail(cvDTO.getEmail());
        cv.setPhone(cvDTO.getPhone());
        cv.setAddress(cvDTO.getAddress());
        cv.setProfilePicture(cvDTO.getProfilePicture());
        cv.setSummary(cvDTO.getSummary());
        cv.setSkills(cvDTO.getSkills());
        cv.setUpdatedAt(LocalDate.now());

        cv.getExperiences().clear();
        if (cvDTO.getExperiences() != null) {
            cvDTO.getExperiences().forEach(expDTO -> {
                Experience exp = new Experience();
                exp.setPosition(expDTO.getPosition());
                exp.setCompany(expDTO.getCompany());
                exp.setLocation(expDTO.getLocation());
                exp.setStartDate(expDTO.getStartDate());
                exp.setEndDate(expDTO.getEndDate());
                exp.setDescription(expDTO.getDescription());
                cv.addExperience(exp);
            });
        }

        cv.getEducations().clear();
        if (cvDTO.getEducations() != null) {
            cvDTO.getEducations().forEach(eduDTO -> {
                Education edu = new Education();
                edu.setDegree(eduDTO.getDegree());
                edu.setInstitution(eduDTO.getInstitution());
                edu.setLocation(eduDTO.getLocation());
                edu.setStartDate(eduDTO.getStartDate());
                edu.setEndDate(eduDTO.getEndDate());
                edu.setDescription(eduDTO.getDescription());
                cv.addEducation(edu);
            });
        }

        cv.getLanguages().clear();
        if (cvDTO.getLanguages() != null) {
            cvDTO.getLanguages().forEach(langDTO -> {
                Language lang = new Language();
                lang.setName(langDTO.getName());
                lang.setLevel(langDTO.getLevel());
                cv.addLanguage(lang);
            });
        }

        CV updatedCV = cvRepository.save(cv);
        return convertToDTO(updatedCV);
    }

    public void deleteCV(UUID cvId, UUID userId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new RuntimeException("CV not found or access denied"));
        cvRepository.delete(cv);
    }

    private CVDTO convertToDTO(CV cv) {
        CVDTO dto = new CVDTO();
        dto.setId(cv.getId());
        dto.setTemplateType(cv.getTemplateType());
        dto.setFirstName(cv.getFirstName());
        dto.setLastName(cv.getLastName());
        dto.setEmail(cv.getEmail());
        dto.setPhone(cv.getPhone());
        dto.setAddress(cv.getAddress());
        dto.setProfilePicture(cv.getProfilePicture());
        dto.setSummary(cv.getSummary());
        dto.setSkills(cv.getSkills());
        dto.setCreatedAt(cv.getCreatedAt());
        dto.setUpdatedAt(cv.getUpdatedAt());

        dto.setExperiences(cv.getExperiences().stream().map(exp -> {
            ExperienceDTO expDTO = new ExperienceDTO();
            expDTO.setId(exp.getId());
            expDTO.setPosition(exp.getPosition());
            expDTO.setCompany(exp.getCompany());
            expDTO.setLocation(exp.getLocation());
            expDTO.setStartDate(exp.getStartDate());
            expDTO.setEndDate(exp.getEndDate());
            expDTO.setDescription(exp.getDescription());
            return expDTO;
        }).collect(Collectors.toList()));

        dto.setEducations(cv.getEducations().stream().map(edu -> {
            EducationDTO eduDTO = new EducationDTO();
            eduDTO.setId(edu.getId());
            eduDTO.setDegree(edu.getDegree());
            eduDTO.setInstitution(edu.getInstitution());
            eduDTO.setLocation(edu.getLocation());
            eduDTO.setStartDate(edu.getStartDate());
            eduDTO.setEndDate(edu.getEndDate());
            eduDTO.setDescription(edu.getDescription());
            return eduDTO;
        }).collect(Collectors.toList()));

        dto.setLanguages(cv.getLanguages().stream().map(lang -> {
            LanguageDTO langDTO = new LanguageDTO();
            langDTO.setId(lang.getId());
            langDTO.setName(lang.getName());
            langDTO.setLevel(lang.getLevel());
            return langDTO;
        }).collect(Collectors.toList()));

        return dto;
    }
}