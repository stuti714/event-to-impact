package com.eventtoimpact.india.dto;

import java.util.List;

public record MlRecommendationRequest(MlUserProfile profile, List<MlEventDocument> events) {}
