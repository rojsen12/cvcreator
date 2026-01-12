package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import java.util.*;

@Data
public class AiSkillsDTO {
    private UUID id;
    private List<String> technical = new ArrayList<>();
    private List<String> soft = new ArrayList<>();

    @JsonCreator
    public static AiSkillsDTO fromAny(Object value) {
        AiSkillsDTO dto = new AiSkillsDTO();
        if (value instanceof List) {
            // Obsługa listy: ["skill1", "skill2"]
            dto.setTechnical((List<String>) value);
        } else if (value instanceof Map) {
            // Obsługa obiektu: {"technical": [], "soft": []}
            Map<String, Object> map = (Map<String, Object>) value;
            dto.setTechnical((List<String>) map.getOrDefault("technical", new ArrayList<>()));
            dto.setSoft((List<String>) map.getOrDefault("soft", new ArrayList<>()));
            // Próba odczytania ID jeśli istnieje
            if (map.containsKey("id") && map.get("id") != null) {
                try { dto.setId(UUID.fromString(map.get("id").toString())); } catch (Exception e) {}
            }
        }
        return dto;
    }
}