package com.eventtoimpact.india.dto;

public record AlertItem(
        String severity,
        String title,
        String message,
        Long eventId,
        String action
) {}
