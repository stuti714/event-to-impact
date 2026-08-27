package com.eventtoimpact.india.dto;

public record MlFeedbackResponse(boolean accepted, int feedbackSamples, String modelMode, String message) {}
