package com.eventtoimpact.india.dto;

import java.util.Map;

public record DashboardInsights(
        int totalEvents,
        int upcomingEvents,
        int freeEvents,
        int verifiedDates,
        int tentativeDates,
        Map<Integer, Long> eventsByYear,
        Map<String, Long> eventsByCategory
) {}
