package com.example.cvcreator.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.UUID;

@Data
public class AiPersonalInfoDTO {
    private UUID id = UUID.randomUUID();
    @JsonAlias({"firstName", "name", "first_name"})
    private String firstName;
    @JsonAlias({"lastName", "surname", "last_name"})
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String linkedin;
    private String github;
}