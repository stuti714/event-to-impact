package com.eventtoimpact.india.service;

import com.eventtoimpact.india.dto.*;
import com.eventtoimpact.india.model.Event;
import com.eventtoimpact.india.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventIntelligenceService {
    private final EventRepository repository;

    public EventIntelligenceService(EventRepository repository) {
        this.repository = repository;
    }

    public List<Event> findEvents(Integer year, String query, String category, Boolean freeOnly) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String c = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        return repository.findAllByOrderByStartTimeAsc().stream()
                .filter(event -> year == null || event.getStartTime().getYear() == year)
                .filter(event -> q.isBlank() || searchableText(event).contains(q))
                .filter(event -> c.isBlank() || event.getCategory().toLowerCase(Locale.ROOT).equals(c))
                .filter(event -> !Boolean.TRUE.equals(freeOnly) || event.isFreeEntry())
                .toList();
    }

    public Event getEvent(long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Event not found"));
    }

    public RiskAssessment assess(Event event) {
        double score;
        List<String> reasons = new ArrayList<>();
        boolean tentativeDate = "TENTATIVE_WINDOW".equals(event.getDateStatus()) || "DATES_TBA".equals(event.getDateStatus());
        boolean localProgrammeUnconfirmed = event.getVerificationStatus() != null
                && event.getVerificationStatus().contains("PROGRAMME");

        if (event.getExpectedAttendance() > 0 && event.getCapacity() > 0) {
            score = Math.min(1.0, (double) event.getExpectedAttendance() / event.getCapacity());
            reasons.add("Uses a declared attendance scenario relative to venue capacity");
        } else {
            score = Math.min(0.72, Math.max(0.12, event.getPopularityScore() / 10.0 * 0.72));
            reasons.add("Based on a curated public-activity band; no live footfall is claimed");
        }

        if (event.isFreeEntry()) {
            score += 0.08;
            reasons.add("Free public participation can increase walk-in interest");
        }
        if (event.isPublicHoliday()) {
            score += 0.10;
            reasons.add("Public-holiday timing can increase participation");
        }
        if (event.getParticipationMode() != null && event.getParticipationMode().contains("Online")) {
            score = Math.max(0.08, score - 0.12);
            reasons.add("An online participation option reduces dependence on a crowded venue");
        }
        if (!tentativeDate) {
            DayOfWeek day = event.getStartTime().getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                score += 0.07;
                reasons.add("Weekend scheduling may increase participation");
            }
            if (!event.isIndoor() && event.getStartTime().getHour() >= 17) {
                score += 0.04;
                reasons.add("Outdoor evening activities can attract more participants");
            }
        }
        if (tentativeDate) {
            reasons.add("The schedule is provisional and must be checked before travel");
        }
        if (localProgrammeUnconfirmed) {
            reasons.add("The annual observance date is fixed, but local programme details must be verified");
        }

        int percentage = (int) Math.round(Math.min(1.0, score) * 100);
        String level = percentage >= 72 ? "HIGH" : percentage >= 43 ? "MODERATE" : "LOW";
        String confidence = confidence(event);
        int minutes = switch (level) { case "HIGH" -> 90; case "MODERATE" -> 60; default -> 30; };
        String arrival = tentativeDate || localProgrammeUnconfirmed
                ? "Check local programme"
                : event.getStartTime().minusMinutes(minutes).format(DateTimeFormatter.ofPattern("h:mm a"));
        String attendanceNote = event.getExpectedAttendance() > 0
                ? "Planning estimate only: " + event.getExpectedAttendance() + " visitors; not a live count."
                : "No exact attendance is predicted. This is a comparative public-activity score, not a people count.";

        return new RiskAssessment(level, percentage, confidence, arrival, reasons, attendanceNote);
    }

    public List<RecommendationResult> recommend(RecommendationRequest request) {
        return recommendAll(request).stream().limit(8).toList();
    }

    public List<RecommendationResult> recommendAll(RecommendationRequest request) {
        RecommendationRequest safe = request == null
                ? new RecommendationRequest(List.of(), 1000, "MODERATE", "FRIENDS", false, "ANY", null, null)
                : request;
        int budget = safe.maxBudget() == null ? 1000 : safe.maxBudget();
        String tolerance = valueOr(safe.crowdTolerance(), "MODERATE");
        String companions = valueOr(safe.companions(), "FRIENDS");
        String environment = valueOr(safe.environment(), "ANY");
        List<String> interests = safe.interests() == null ? List.of() : safe.interests().stream()
                .map(value -> value.toLowerCase(Locale.ROOT)).toList();

        return repository.findAllByOrderByStartTimeAsc().stream()
                .filter(event -> safe.from() == null || !event.getEndTime().toLocalDate().isBefore(safe.from()))
                .filter(event -> safe.to() == null || !event.getStartTime().toLocalDate().isAfter(safe.to()))
                .filter(event -> !Boolean.TRUE.equals(safe.accessibleOnly()) || event.isAccessible())
                .map(event -> rank(event, interests, budget, tolerance, companions, environment))
                .sorted(Comparator.comparingInt(RecommendationResult::matchScore).reversed()
                        .thenComparing(result -> result.event().getStartTime()))
                .toList();
    }

    public DashboardInsights insights() {
        List<Event> events = repository.findAllByOrderByStartTimeAsc();
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, Long> byYear = events.stream().collect(Collectors.groupingBy(
                event -> event.getStartTime().getYear(), TreeMap::new, Collectors.counting()));
        Map<String, Long> byCategory = events.stream().collect(Collectors.groupingBy(
                Event::getCategory, TreeMap::new, Collectors.counting()));
        return new DashboardInsights(
                events.size(),
                (int) events.stream().filter(event -> event.getEndTime().isAfter(now)).count(),
                (int) events.stream().filter(Event::isFreeEntry).count(),
                (int) events.stream().filter(event -> Set.of("VERIFIED", "DATE_VERIFIED", "FIXED_ANNUAL_DATE").contains(event.getDateStatus())).count(),
                (int) events.stream().filter(event -> event.getStartTime().getYear() == 2027
                        && event.getVerificationStatus() != null
                        && event.getVerificationStatus().contains("DETAILS_TBA")).count(),
                byYear,
                byCategory
        );
    }

    public List<AlertItem> alerts() {
        LocalDateTime now = LocalDateTime.now();
        List<AlertItem> alerts = new ArrayList<>();
        repository.findAllByOrderByStartTimeAsc().stream()
                .filter(event -> event.getEndTime().isAfter(now))
                .forEach(event -> {
                    long days = Duration.between(now, event.getStartTime()).toDays();
                    if (days >= 0 && days <= 14) {
                        alerts.add(new AlertItem("INFO", "Event approaching", event.getTitle() + " begins in " + Math.max(0, days) + " day(s).", event.getId(), "Open event"));
                    }
                    if (assess(event).level().equals("HIGH") && days >= 0 && days <= 90) {
                        alerts.add(new AlertItem("WARNING", "High public-activity band", event.getTitle() + " may attract broad participation. Check the local organiser before attending.", event.getId(), "View reasons"));
                    }
                    if (event.getStartTime().getYear() == 2027
                            && event.getVerificationStatus() != null
                            && event.getVerificationStatus().contains("DETAILS_TBA")) {
                        alerts.add(new AlertItem("VERIFY", "2027 programme needs verification",
                                event.getTitle() + " has a fixed annual date, but local programme details are not yet confirmed.",
                                event.getId(), "Check source"));
                    }
                });
        return alerts.stream().limit(12).toList();
    }

    private RecommendationResult rank(Event event, List<String> interests, int budget, String tolerance,
                                      String companions, String environment) {
        int score = 28;
        List<String> reasons = new ArrayList<>();
        String text = searchableText(event);
        long matched = interests.stream()
                .flatMap(value -> Arrays.stream(value.split("\\s+")))
                .map(String::trim)
                .filter(value -> value.length() > 2)
                .distinct()
                .filter(text::contains)
                .count();
        if (matched > 0) {
            score += Math.min(30, (int) matched * 15);
            reasons.add("Matches " + matched + " selected topic" + (matched > 1 ? "s" : ""));
        }
        if (event.isFreeEntry() || event.getPrice() <= budget) {
            score += 15;
            reasons.add(event.isFreeEntry() ? "Free public participation" : "Within your ₹" + budget + " budget");
        } else {
            score -= 18;
        }
        RiskAssessment risk = assess(event);
        if (riskFits(risk.level(), tolerance)) {
            score += 15;
            reasons.add(risk.level().toLowerCase(Locale.ROOT) + " public-activity level fits your preference");
        } else {
            score -= 12;
        }
        if ((companions.equalsIgnoreCase("FAMILY") && event.isFamilyFriendly())
                || (companions.equalsIgnoreCase("STUDENTS") && event.isStudentFriendly())
                || companions.equalsIgnoreCase("FRIENDS") || companions.equalsIgnoreCase("SOLO")) {
            score += 8;
            reasons.add("Suitable for " + companions.toLowerCase(Locale.ROOT));
        }
        if (environment.equalsIgnoreCase("INDOOR") && event.isIndoor()
                || environment.equalsIgnoreCase("OUTDOOR") && !event.isIndoor()
                || environment.equalsIgnoreCase("ANY")) {
            score += 4;
        }
        if (Set.of("VERIFIED", "DATE_VERIFIED", "FIXED_ANNUAL_DATE").contains(event.getDateStatus())) score += 5;
        return new RecommendationResult(event, Math.max(0, Math.min(100, score)), reasons, risk);
    }

    private boolean riskFits(String level, String tolerance) {
        int risk = switch (level) { case "HIGH" -> 3; case "MODERATE" -> 2; default -> 1; };
        int accepted = switch (tolerance.toUpperCase(Locale.ROOT)) { case "HIGH" -> 3; case "MODERATE" -> 2; default -> 1; };
        return risk <= accepted;
    }

    private String confidence(Event event) {
        if (Set.of("TENTATIVE_WINDOW", "DATES_TBA").contains(event.getDateStatus())) return "LOW";
        if (event.getVerificationStatus() != null && event.getVerificationStatus().contains("DETAILS_TBA")) return "LOW";
        if (event.getExpectedAttendance() > 0 && "ORGANIZER_ESTIMATE".equals(event.getAttendanceBasis())) return "HIGH";
        return "MEDIUM";
    }

    private String searchableText(Event event) {
        return String.join(" ", nullSafe(event.getTitle()), nullSafe(event.getCategory()), nullSafe(event.getDescription()),
                nullSafe(event.getVenue()), nullSafe(event.getArea()), nullSafe(event.getTags()), nullSafe(event.getAudience()),
                nullSafe(event.getParticipationMode()), nullSafe(event.getImpactGoal())).toLowerCase(Locale.ROOT);
    }

    private String nullSafe(String value) { return value == null ? "" : value; }
    private String valueOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}
