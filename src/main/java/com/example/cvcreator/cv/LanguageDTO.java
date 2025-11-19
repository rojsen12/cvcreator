package com.example.cvcreator.cv;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LanguageDTO {
    private UUID id;
    private String name;
    private String level;
}