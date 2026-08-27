package com.eventtoimpact.india.service;

import com.eventtoimpact.india.dto.*;
import com.eventtoimpact.india.model.Event;
import com.eventtoimpact.india.model.UserFeedback;
import com.eventtoimpact.india.repository.UserFeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class HybridRecommendationService {
    private static final Set<String> FEEDBACK_ACTIONS = Set.of("INTERESTED", "SAVED", "NOT_FOR_ME");

    private final EventIntelligenceService rules;
    private final MlRecommendationClient mlClient;
    private final UserFeedbackRepository feedbackRepository;

    public HybridRecommendationService(EventIntelligenceService rules, MlRecommendationClient mlClient,
                                       UserFeedbackRepository feedbackRepository) {
        this.rules = rules;
        this.mlClient = mlClient;
        this.feedbackRepository = feedbackRepository;
    }

    public List<RecommendationResult> recommend(RecommendationRequest request) {
        RecommendationRequest safe = safeRequest(request);
        List<RecommendationResult> baseline = rules.recommendAll(safe);
        if (baseline.isEmpty()) return List.of();

        try {
            MlRecommendationRequest mlRequest = new MlRecommendationRequest(
                    profile(safe), baseline.stream().map(this::document).toList());
            MlRecommendationResponse response = Objects.requireNonNull(mlClient.recommend(mlRequest));
            Map<Long, MlEventScore> byEvent = new HashMap<>();
            if (response.scores() != null) {
                response.scores().forEach(score -> byEvent.put(score.eventId(), score));
            }
            return baseline.stream()
                    .map(result -> applyMl(result, byEvent.get(result.event().getId()), response))
                    .sorted(Comparator.comparingInt(RecommendationResult::matchScore).reversed()
                            .thenComparing(result -> result.event().getStartTime()))
                    .limit(8)
                    .toList();
        } catch (RuntimeException exception) {
            return baseline.stream().limit(8).map(this::fallback).toList();
        }
    }

    public FeedbackResponse recordFeedback(FeedbackRequest request, String username) {
        if (request == null || !FEEDBACK_ACTIONS.contains(normalize(request.action()))) {
            throw new IllegalArgumentException("Feedback action must be INTERESTED, SAVED or NOT_FOR_ME");
        }
        Event event = rules.getEvent(request.eventId());
        RecommendationRequest safe = safeRequest(request.preferences());
        UserFeedback stored = new UserFeedback();
        stored.setEventId(event.getId());
        stored.setUsername(username == null || username.isBlank() ? "visitor" : username);
        stored.setAction(normalize(request.action()));
        stored.setProfileSummary(String.join(",", safe.interests() == null ? List.of() : safe.interests())
                + " | budget=" + safe.maxBudget() + " | crowd=" + safe.crowdTolerance());
        stored.setCreatedAt(LocalDateTime.now());
        feedbackRepository.save(stored);
        long count = feedbackRepository.countByUsername(stored.getUsername());

        try {
            RiskAssessment risk = rules.assess(event);
            MlFeedbackResponse ml = Objects.requireNonNull(mlClient.feedback(
                    new MlFeedbackRequest(stored.getAction(), profile(safe), document(event, risk))));
            return new FeedbackResponse(true, count, ml.modelMode(), ml.message());
        } catch (RuntimeException exception) {
            return new FeedbackResponse(true, count, "RULE_FALLBACK",
                    "Feedback saved in Event to Impact. Start the ML service to use it for model learning.");
        }
    }

    public ModelCardResponse modelCard() {
        try {
            return Objects.requireNonNullElseGet(mlClient.modelCard(), ModelCardResponse::offline);
        } catch (RuntimeException exception) {
            return ModelCardResponse.offline();
        }
    }

    private RecommendationResult applyMl(RecommendationResult baseline, MlEventScore score,
                                         MlRecommendationResponse response) {
        if (score == null) return fallback(baseline);
        List<String> reasons = new ArrayList<>(score.reasons() == null ? List.of() : score.reasons());
        if (reasons.size() < 3) baseline.matchReasons().stream().limit(3 - reasons.size()).forEach(reasons::add);
        return new RecommendationResult(
                baseline.event(), score.score(), reasons, baseline.risk(),
                response.modelMode(), score.contentScore(), score.constraintScore(), score.learnedScore(),
                response.modelVersion());
    }

    private RecommendationResult fallback(RecommendationResult result) {
        return new RecommendationResult(
                result.event(), result.matchScore(), result.matchReasons(), result.risk(),
                "RULE_FALLBACK", 0, result.matchScore(), null, "rules-v1");
    }

    private MlEventDocument document(RecommendationResult result) {
        return document(result.event(), result.risk());
    }

    private MlEventDocument document(Event event, RiskAssessment risk) {
        return new MlEventDocument(
                event.getId(), event.getTitle(), event.getCategory(), event.getDescription(), event.getTags(),
                event.getVenue(), event.getArea(), event.getAudience(), event.getParticipationMode(), event.getImpactGoal(),
                event.getPrice(), event.isFreeEntry(), event.isIndoor(),
                event.isFamilyFriendly(), event.isStudentFriendly(), event.isAccessible(), event.getDateStatus(), risk.level());
    }

    private MlUserProfile profile(RecommendationRequest request) {
        return new MlUserProfile(
                request.interests() == null ? List.of() : request.interests(),
                request.maxBudget() == null ? 1000 : request.maxBudget(),
                valueOr(request.crowdTolerance(), "MODERATE"), valueOr(request.companions(), "FRIENDS"),
                Boolean.TRUE.equals(request.accessibleOnly()), valueOr(request.environment(), "ANY"),
                request.from(), request.to());
    }

    private RecommendationRequest safeRequest(RecommendationRequest request) {
        return request == null
                ? new RecommendationRequest(List.of(), 1000, "MODERATE", "FRIENDS", false, "ANY", null, null)
                : request;
    }

    private String normalize(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT); }
    private String valueOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}
