package com.eventtoimpact.india.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String category;
    @Column(length = 1600) private String description;
    @Column(nullable = false) private String venue;
    private String area;
    private String city = "India";
    @Column(nullable = false) private LocalDateTime startTime;
    @Column(nullable = false) private LocalDateTime endTime;
    private String recurringRule;
    private int price;
    private boolean freeEntry;
    private boolean indoor;
    private int capacity;
    private int expectedAttendance;
    private String attendanceBasis;
    private int popularityScore;
    private boolean familyFriendly;
    private boolean studentFriendly;
    private boolean seniorFriendly;
    private boolean accessible;
    private String noiseLevel;
    private String walkingLevel;
    private String tags;
    private String audience;
    private String participationMode;
    @Column(length = 1000) private String impactGoal;
    private boolean publicHoliday;
    private boolean featured;
    private String sourceName;
    @Column(length = 1000) private String sourceUrl;
    private LocalDateTime verifiedAt;
    private String verificationStatus;
    private String dateStatus;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getRecurringRule() { return recurringRule; }
    public void setRecurringRule(String recurringRule) { this.recurringRule = recurringRule; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public boolean isFreeEntry() { return freeEntry; }
    public void setFreeEntry(boolean freeEntry) { this.freeEntry = freeEntry; }
    public boolean isIndoor() { return indoor; }
    public void setIndoor(boolean indoor) { this.indoor = indoor; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getExpectedAttendance() { return expectedAttendance; }
    public void setExpectedAttendance(int expectedAttendance) { this.expectedAttendance = expectedAttendance; }
    public String getAttendanceBasis() { return attendanceBasis; }
    public void setAttendanceBasis(String attendanceBasis) { this.attendanceBasis = attendanceBasis; }
    public int getPopularityScore() { return popularityScore; }
    public void setPopularityScore(int popularityScore) { this.popularityScore = popularityScore; }
    public boolean isFamilyFriendly() { return familyFriendly; }
    public void setFamilyFriendly(boolean familyFriendly) { this.familyFriendly = familyFriendly; }
    public boolean isStudentFriendly() { return studentFriendly; }
    public void setStudentFriendly(boolean studentFriendly) { this.studentFriendly = studentFriendly; }
    public boolean isSeniorFriendly() { return seniorFriendly; }
    public void setSeniorFriendly(boolean seniorFriendly) { this.seniorFriendly = seniorFriendly; }
    public boolean isAccessible() { return accessible; }
    public void setAccessible(boolean accessible) { this.accessible = accessible; }
    public String getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(String noiseLevel) { this.noiseLevel = noiseLevel; }
    public String getWalkingLevel() { return walkingLevel; }
    public void setWalkingLevel(String walkingLevel) { this.walkingLevel = walkingLevel; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getParticipationMode() { return participationMode; }
    public void setParticipationMode(String participationMode) { this.participationMode = participationMode; }
    public String getImpactGoal() { return impactGoal; }
    public void setImpactGoal(String impactGoal) { this.impactGoal = impactGoal; }
    public boolean isPublicHoliday() { return publicHoliday; }
    public void setPublicHoliday(boolean publicHoliday) { this.publicHoliday = publicHoliday; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getDateStatus() { return dateStatus; }
    public void setDateStatus(String dateStatus) { this.dateStatus = dateStatus; }
}
