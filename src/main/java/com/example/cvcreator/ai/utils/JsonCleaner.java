package com.example.cvcreator.ai.utils;

public class JsonCleaner {

    public static String clean(String rawResponse) {
        if (rawResponse == null) {
            return "{}";
        }

        String cleaned = rawResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        if (!cleaned.startsWith("{") && !cleaned.startsWith("[")) {
            int start = cleaned.indexOf("{");
            int arrayStart = cleaned.indexOf("[");

            if (start != -1 && (arrayStart == -1 || start < arrayStart)) {
                cleaned = cleaned.substring(start);
            } else if (arrayStart != -1) {
                cleaned = cleaned.substring(arrayStart);
            }
        }

        return cleaned;
    }
}