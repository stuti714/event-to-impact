package com.eventtoimpact.india.service;

import com.eventtoimpact.india.dto.*;
import com.eventtoimpact.india.model.Event;
import com.eventtoimpact.india.repository.EventRepository;
import com.eventtoimpact.india.repository.UserFeedbackRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridRecommendationServiceTest {
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final UserFeedbackRepository feedbackRepository = mock(UserFeedbackRepository.class);
    private final MlRecommendationClient mlClient = mock(MlRecommendationClient.class);
    private final EventIntelligenceService rules = new EventIntelligenceService(eventRepository);
    private final HybridRecommendationService service = new HybridRecommendationService(rules, mlClient, feedbackRepository);

    @Test
    void appliesContentModelScoresAndExposesModelMode() {
        Event education = event(1L, "National Science Day", "Education", "science,education,students");
        Event environment = event(2L, "World Water Day", "Environment", "water,environment");
        when(eventRepository.findAllByOrderByStartTimeAsc()).thenReturn(List.of(education, environment));
        when(mlClient.recommend(any())).thenReturn(new MlRecommendationResponse(
                "CONTENT_BASED", "event-to-impact-ml-1.0",
                List.of(
                        new MlEventScore(1, 84, 82, 89, null, List.of("Content model matched: science education")),
                        new MlEventScore(2, 31, 12, 76, null, List.of("Constraint fit supports this option")))));

        var request = new RecommendationRequest(List.of("science education"), 0, "MODERATE", "STUDENTS", false, "ANY", null, null);
        var results = service.recommend(request);

        assertThat(results.get(0).event().getTitle()).isEqualTo("National Science Day");
        assertThat(results.get(0).modelMode()).isEqualTo("CONTENT_BASED");
        assertThat(results.get(0).contentScore()).isEqualTo(82);
    }

    @Test
    void fallsBackToRulesWhenMlServiceIsUnavailable() {
        Event safety = event(1L, "Cyber Security Awareness Month", "Safety", "cybersecurity,safety");
        when(eventRepository.findAllByOrderByStartTimeAsc()).thenReturn(List.of(safety));
        when(mlClient.recommend(any())).thenThrow(new RuntimeException("offline"));

        var results = service.recommend(null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).modelMode()).isEqualTo("RULE_FALLBACK");
    }

    private Event event(long id, String title, String category, String tags) {
        Event event = new Event();
        try {
            Field field = Event.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(event, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        event.setTitle(title);
        event.setCategory(category);
        event.setDescription(title);
        event.setVenue("India");
        event.setArea("Central");
        event.setStartTime(LocalDateTime.of(2026, 8, 15, 10, 0));
        event.setEndTime(LocalDateTime.of(2026, 8, 15, 18, 0));
        event.setTags(tags);
        event.setAudience("Students and citizens");
        event.setParticipationMode("Online + campus");
        event.setImpactGoal("Complete one useful awareness action");
        event.setPopularityScore(5);
        event.setDateStatus("DATE_VERIFIED");
        event.setAccessible(true);
        return event;
    }
}
