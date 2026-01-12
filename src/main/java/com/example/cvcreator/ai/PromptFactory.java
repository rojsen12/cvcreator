package com.example.cvcreator.ai;

public class PromptFactory {

    public static String build(AiCvRequestDTO req) {
        return switch (req.getMode()) {
            case GENERATE -> GenerateCvPrompt.build(req);
            case IMPROVE -> ImproveCvPrompt.build(req);
            case JOB_FIT -> JobFitCvPrompt.build(req);
        };
    }
}
