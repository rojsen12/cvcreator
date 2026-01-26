package com.example.cvcreator.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiCVRepository extends JpaRepository<CVDocument, Long> {
    Optional<CVDocument> findByUserId(UUID userId);
}