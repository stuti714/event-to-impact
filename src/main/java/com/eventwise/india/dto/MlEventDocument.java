package com.eventtoimpact.india.dto;

public record MlEventDocument(
        long id,
        String title,
        String category,
        String description,
        String tags,
        String venue,
        String area,
        String audience,
        String participationMode,
        String impactGoal,
        int price,
        boolean freeEntry,
        boolean indoor,
        boolean familyFriendly,
        boolean studentFriendly,
        boolean accessible,
        String dateStatus,
        String crowdRisk
) {}
