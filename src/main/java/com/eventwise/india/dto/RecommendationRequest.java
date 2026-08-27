package com.eventtoimpact.india.dto;

import java.time.LocalDate;
import java.util.List;

public record RecommendationRequest(
        List<String> interests,
        Integer maxBudget,
        String crowdTolerance,
        String companions,
        Boolean accessibleOnly,
        String environment,
        LocalDate from,
        LocalDate to
) {}
