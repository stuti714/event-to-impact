package com.eventtoimpact.india.service;

import com.eventtoimpact.india.dto.RecommendationRequest;
import com.eventtoimpact.india.dto.RiskAssessment;
import com.eventtoimpact.india.model.Event;
import com.eventtoimpact.india.repository.EventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventIntelligenceServiceTest {
    private final EventRepository repository = mock(EventRepository.class);
    private final EventIntelligenceService service = new EventIntelligenceService(repository);

    @Test
    void riskAssessmentNeverClaimsAnExactCountWithoutAttendanceData() {
        Event event = sampleEvent("Republic Day Civic Programme", "National", 10, true, true, "FIXED_ANNUAL_DATE");

        RiskAssessment result = service.assess(event);

        assertThat(result.level()).isEqualTo("HIGH");
        assertThat(result.attendanceNote()).contains("No exact attendance is predicted").contains("not a people count");
        assertThat(result.reasons()).anyMatch(reason -> reason.contains("no live footfall"));
    }

    @Test
    void tentativeDatesReduceConfidence() {
        Event event = sampleEvent("World Health Day 2027", "Health", 9, true, false, "DATES_TBA");

        RiskAssessment result = service.assess(event);

        assertThat(result.confidence()).isEqualTo("LOW");
        assertThat(result.reasons()).anyMatch(reason -> reason.contains("provisional"));
    }

    @Test
    void recommendationRewardsImpactInterestAndPracticalFit() {
        Event skills = sampleEvent("World Youth Skills Day", "Career", 4, true, false, "FIXED_ANNUAL_DATE");
        skills.setTags("career,skills,youth,students");
        skills.setAudience("Students and job seekers");
        skills.setImpactGoal("Complete a skill audit and attend a learning activity");
        Event unrelated = sampleEvent("Water Conservation Session", "Environment", 9, true, false, "FIXED_ANNUAL_DATE");
        unrelated.setTags("water,environment");
        when(repository.findAllByOrderByStartTimeAsc()).thenReturn(List.of(unrelated, skills));

        var request = new RecommendationRequest(List.of("career skills"), 0, "MODERATE", "STUDENTS",
                false, "ANY", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        var results = service.recommend(request);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).event().getTitle()).isEqualTo("World Youth Skills Day");
        assertThat(results.get(0).matchReasons()).anyMatch(reason -> reason.contains("selected topic"));
    }

    private Event sampleEvent(String title, String category, int popularity, boolean free, boolean holiday, String dateStatus) {
        Event event = new Event();
        event.setTitle(title);
        event.setCategory(category);
        event.setDescription("Test event");
        event.setVenue("India");
        event.setArea("Central");
        event.setStartTime(LocalDateTime.of(2026, 8, 15, 18, 0));
        event.setEndTime(LocalDateTime.of(2026, 8, 15, 21, 0));
        event.setPopularityScore(popularity);
        event.setFreeEntry(free);
        event.setPublicHoliday(holiday);
        event.setDateStatus(dateStatus);
        event.setAttendanceBasis("NO_LIVE_COUNT");
        event.setParticipationMode("Online + campus");
        event.setTags(category.toLowerCase());
        event.setAccessible(true);
        return event;
    }
}
