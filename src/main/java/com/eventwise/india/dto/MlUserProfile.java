package com.eventtoimpact.india.dto;

import java.time.LocalDate;
import java.util.List;

public record MlUserProfile(
        List<String> interests,
        int maxBudget,
        String crowdTolerance,
        String companions,
        boolean accessibleOnly,
        String environment,
        LocalDate fromDate,
        LocalDate toDate
) {}
