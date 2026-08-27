package com.eventtoimpact.india.dto;

import java.util.List;

public record MlEventScore(
        long eventId,
        int score,
        int contentScore,
        int constraintScore,
        Integer learnedScore,
        List<String> reasons
) {}
