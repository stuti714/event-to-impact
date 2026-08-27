package com.eventtoimpact.india.dto;

public record FeedbackRequest(long eventId, String action, RecommendationRequest preferences) {}
