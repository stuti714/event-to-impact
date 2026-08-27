package com.eventtoimpact.india.dto;

public record FeedbackResponse(
        boolean accepted,
        long storedFeedback,
        String modelMode,
        String message
) {}
