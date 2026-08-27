package com.eventtoimpact.india.controller;

import com.eventtoimpact.india.dto.*;
import com.eventtoimpact.india.model.Event;
import com.eventtoimpact.india.service.EventIntelligenceService;
import com.eventtoimpact.india.service.HybridRecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class EventApiController {
    private final EventIntelligenceService service;
    private final HybridRecommendationService recommendations;

    public EventApiController(EventIntelligenceService service, HybridRecommendationService recommendations) {
        this.service = service;
        this.recommendations = recommendations;
    }

    @GetMapping("/events")
    public List<Event> events(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean freeOnly) {
        return service.findEvents(year, q, category, freeOnly);
    }

    @GetMapping("/events/{id}")
    public Event event(@PathVariable long id) { return service.getEvent(id); }

    @GetMapping("/events/{id}/risk")
    public RiskAssessment risk(@PathVariable long id) { return service.assess(service.getEvent(id)); }

    @GetMapping("/insights")
    public DashboardInsights insights() { return service.insights(); }

    @GetMapping("/alerts")
    public List<AlertItem> alerts() { return service.alerts(); }

    @PostMapping("/recommendations")
    public List<RecommendationResult> recommendations(@RequestBody(required = false) RecommendationRequest request) {
        return recommendations.recommend(request);
    }

    @PostMapping("/feedback")
    public FeedbackResponse feedback(@RequestBody FeedbackRequest request, Principal principal) {
        return recommendations.recordFeedback(request, principal == null ? "visitor" : principal.getName());
    }

    @GetMapping("/model-card")
    public ModelCardResponse modelCard() { return recommendations.modelCard(); }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "Event to Impact", "timestamp", Instant.now());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public Map<String, String> notFound(NoSuchElementException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> badRequest(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }
}
