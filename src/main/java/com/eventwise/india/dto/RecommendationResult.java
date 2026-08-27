package com.eventtoimpact.india.dto;

import com.eventtoimpact.india.model.Event;
import java.util.List;

public record RecommendationResult(
        Event event,
        int matchScore,
        List<String> matchReasons,
        RiskAssessment risk,
        String modelMode,
        int contentScore,
        int constraintScore,
        Integer learnedScore,
        String modelVersion
) {
    public RecommendationResult(Event event, int matchScore, List<String> matchReasons, RiskAssessment risk) {
        this(event, matchScore, matchReasons, risk, "RULE_BASELINE", 0, matchScore, null, "rules-v1");
    }
}
