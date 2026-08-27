package com.eventtoimpact.india.dto;

import java.util.List;

public record RiskAssessment(
        String level,
        int score,
        String confidence,
        String bestArrivalTime,
        List<String> reasons,
        String attendanceNote
) {}
