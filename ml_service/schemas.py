from __future__ import annotations

from datetime import date, datetime
from typing import Literal

from pydantic import BaseModel, Field


class UserProfile(BaseModel):
    interests: list[str] = Field(default_factory=list)
    maxBudget: int = 1000
    crowdTolerance: str = "MODERATE"
    companions: str = "FRIENDS"
    accessibleOnly: bool = False
    environment: str = "ANY"
    fromDate: date | None = None
    toDate: date | None = None


class EventDocument(BaseModel):
    id: int
    title: str
    category: str
    description: str = ""
    tags: str = ""
    venue: str = ""
    area: str = ""
    audience: str = ""
    participationMode: str = ""
    impactGoal: str = ""
    price: int = 0
    freeEntry: bool = False
    indoor: bool = False
    familyFriendly: bool = False
    studentFriendly: bool = False
    accessible: bool = False
    dateStatus: str = ""
    crowdRisk: str = "MODERATE"


class RecommendationRequest(BaseModel):
    profile: UserProfile
    events: list[EventDocument]


class EventScore(BaseModel):
    eventId: int
    score: int
    contentScore: int
    constraintScore: int
    learnedScore: int | None = None
    reasons: list[str]


class RecommendationResponse(BaseModel):
    modelMode: str
    modelVersion: str
    scores: list[EventScore]


class FeedbackRequest(BaseModel):
    action: Literal["INTERESTED", "SAVED", "NOT_FOR_ME"]
    profile: UserProfile
    event: EventDocument


class FeedbackResponse(BaseModel):
    accepted: bool
    feedbackSamples: int
    modelMode: str
    message: str


class ModelCard(BaseModel):
    status: str
    modelMode: str
    modelVersion: str
    algorithm: str
    indexedEvents: int
    vocabularySize: int
    feedbackSamples: int
    minimumFeedback: int
    trainedAt: datetime | None
    catalogCoverage: float
    supervisedMetric: str
    limitations: list[str]
