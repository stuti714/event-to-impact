package com.eventtoimpact.india.dto;

import java.util.List;

public record MlRecommendationResponse(String modelMode, String modelVersion, List<MlEventScore> scores) {}
