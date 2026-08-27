package com.eventtoimpact.india.dto;

import java.util.List;

public record ModelCardResponse(
        String status,
        String modelMode,
        String modelVersion,
        String algorithm,
        int indexedEvents,
        int vocabularySize,
        int feedbackSamples,
        int minimumFeedback,
        String trainedAt,
        double catalogCoverage,
        String supervisedMetric,
        List<String> limitations
) {
    public static ModelCardResponse offline() {
        return new ModelCardResponse(
                "OFFLINE", "RULE_FALLBACK", "rules-v1",
                "Transparent rules fallback; start the Python ML service for TF-IDF ranking",
                0, 0, 0, 10, null, 0,
                "Unavailable while the ML service is offline",
                List.of("The product remains usable, but recommendations use the explainable rules baseline."));
    }
}
