from __future__ import annotations

import hashlib
import json
import threading
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import joblib
import numpy as np
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.pipeline import Pipeline

from .schemas import (
    EventDocument,
    EventScore,
    FeedbackRequest,
    FeedbackResponse,
    ModelCard,
    RecommendationRequest,
    RecommendationResponse,
    UserProfile,
)


class RecommendationEngine:
    """Content recommender with an optional feedback-trained preference layer."""

    VERSION = "event-to-impact-ml-1.0"

    def __init__(self, storage_dir: Path | None = None, minimum_feedback: int = 10):
        self.storage_dir = storage_dir or Path(__file__).resolve().parent / "runtime"
        self.storage_dir.mkdir(parents=True, exist_ok=True)
        self.feedback_file = self.storage_dir / "feedback.jsonl"
        self.preference_model_file = self.storage_dir / "preference_model.joblib"
        self.minimum_feedback = minimum_feedback
        self._lock = threading.RLock()
        self._content_fingerprint = ""
        self._vectorizer: TfidfVectorizer | None = None
        self._event_matrix = None
        self._event_ids: list[int] = []
        self._trained_at: datetime | None = None
        self._preference_model: Pipeline | None = self._load_preference_model()
        self._indexed_events = 0
        self._vocabulary_size = 0
        self._coverage = 0.0

    def recommend(self, request: RecommendationRequest) -> RecommendationResponse:
        if not request.events:
            return RecommendationResponse(modelMode=self.mode, modelVersion=self.VERSION, scores=[])

        with self._lock:
            self._fit_content_model(request.events)
            query = self._profile_document(request.profile)
            query_vector = self._vectorizer.transform([query])
            similarities = cosine_similarity(query_vector, self._event_matrix).ravel()
            self._coverage = round(float(np.count_nonzero(similarities) / len(similarities)), 3)

            scores: list[EventScore] = []
            for event, similarity in zip(request.events, similarities, strict=True):
                content_score = float(np.clip(similarity, 0.0, 1.0))
                constraint_score, constraint_reasons = self._constraint_fit(request.profile, event)
                learned_score = self._learned_probability(request.profile, event, content_score, constraint_score)

                if learned_score is None:
                    combined = 0.70 * content_score + 0.30 * constraint_score
                else:
                    combined = 0.55 * content_score + 0.25 * constraint_score + 0.20 * learned_score

                reasons = self._content_reasons(request.profile, event, content_score) + constraint_reasons
                if learned_score is not None:
                    reasons.append("Your saved feedback contributes to this ranking")
                scores.append(EventScore(
                    eventId=event.id,
                    score=int(round(np.clip(combined, 0.0, 1.0) * 100)),
                    contentScore=int(round(content_score * 100)),
                    constraintScore=int(round(constraint_score * 100)),
                    learnedScore=None if learned_score is None else int(round(learned_score * 100)),
                    reasons=reasons[:5],
                ))

            scores.sort(key=lambda item: (-item.score, item.eventId))
            return RecommendationResponse(modelMode=self.mode, modelVersion=self.VERSION, scores=scores)

    def record_feedback(self, request: FeedbackRequest) -> FeedbackResponse:
        record = {
            "recordedAt": datetime.now(timezone.utc).isoformat(),
            "label": 0 if request.action == "NOT_FOR_ME" else 1,
            "action": request.action,
            "profile": request.profile.model_dump(mode="json"),
            "event": request.event.model_dump(mode="json"),
        }
        with self._lock:
            with self.feedback_file.open("a", encoding="utf-8") as handle:
                handle.write(json.dumps(record, ensure_ascii=False) + "\n")
            trained = self._train_preference_model()
            records = self._read_feedback()
            samples = len(records)
        message = "Feedback saved. Content-based recommendations remain active."
        if trained:
            message = "Feedback saved. The learned preference layer is now active."
        elif samples < self.minimum_feedback:
            message = f"Feedback saved. {self.minimum_feedback - samples} more sample(s) are needed before supervised learning can activate."
        elif len({int(record["label"]) for record in records}) < 2:
            message = "Feedback saved. Add both a positive choice and a Not-for-me label before supervised learning can activate."
        return FeedbackResponse(accepted=True, feedbackSamples=samples, modelMode=self.mode, message=message)

    def model_card(self) -> ModelCard:
        return ModelCard(
            status="READY",
            modelMode=self.mode,
            modelVersion=self.VERSION,
            algorithm="TF-IDF (1–2 grams) + cosine similarity; logistic regression after real feedback",
            indexedEvents=self._indexed_events,
            vocabularySize=self._vocabulary_size,
            feedbackSamples=len(self._read_feedback()),
            minimumFeedback=self.minimum_feedback,
            trainedAt=self._trained_at,
            catalogCoverage=self._coverage,
            supervisedMetric="Not reported until enough real labeled feedback exists for a held-out evaluation",
            limitations=[
                "The model ranks listed events; it does not predict live attendance.",
                "Cold-start ranking depends on event descriptions and selected interests.",
                "Logistic regression activates only after both positive and negative feedback exist.",
            ],
        )

    @property
    def mode(self) -> str:
        return "HYBRID_LEARNED" if self._preference_model is not None else "CONTENT_BASED"

    def _fit_content_model(self, events: list[EventDocument]) -> None:
        documents = [self._event_document(event) for event in events]
        fingerprint = hashlib.sha256("\n".join(documents).encode("utf-8")).hexdigest()
        if fingerprint == self._content_fingerprint:
            return
        vectorizer = TfidfVectorizer(
            lowercase=True,
            stop_words="english",
            ngram_range=(1, 2),
            sublinear_tf=True,
            min_df=1,
        )
        matrix = vectorizer.fit_transform(documents)
        self._vectorizer = vectorizer
        self._event_matrix = matrix
        self._event_ids = [event.id for event in events]
        self._content_fingerprint = fingerprint
        self._indexed_events = len(events)
        self._vocabulary_size = len(vectorizer.vocabulary_)

    def _event_document(self, event: EventDocument) -> str:
        return " ".join(filter(None, [
            event.title, event.category, event.category,
            event.description, event.tags, event.tags,
            event.audience, event.audience, event.impactGoal,
            event.participationMode, event.venue, event.area,
        ]))

    def _profile_document(self, profile: UserProfile) -> str:
        interests = " ".join(profile.interests or ["education", "awareness", "india"])
        return f"{interests} {interests} {profile.companions} {profile.environment} india awareness event"

    def _constraint_fit(self, profile: UserProfile, event: EventDocument) -> tuple[float, list[str]]:
        checks: list[float] = []
        reasons: list[str] = []

        budget_fit = 1.0 if event.freeEntry or event.price <= profile.maxBudget else max(0.0, profile.maxBudget / max(event.price, 1))
        checks.append(budget_fit)
        if budget_fit == 1.0:
            reasons.append("Free public participation" if event.freeEntry else f"Within your ₹{profile.maxBudget} budget")

        risk_order = {"LOW": 1, "MODERATE": 2, "HIGH": 3}
        risk_fit = 1.0 if risk_order.get(event.crowdRisk.upper(), 2) <= risk_order.get(profile.crowdTolerance.upper(), 2) else 0.2
        checks.append(risk_fit)
        if risk_fit == 1.0:
            reasons.append(f"{event.crowdRisk.lower()} public-activity level fits your preference")

        companion = profile.companions.upper()
        companion_fit = 1.0
        if companion == "FAMILY":
            companion_fit = 1.0 if event.familyFriendly else 0.4
        elif companion == "STUDENTS":
            companion_fit = 1.0 if event.studentFriendly else 0.4
        checks.append(companion_fit)
        if companion_fit == 1.0:
            reasons.append(f"Suitable for {companion.lower()}")

        environment = profile.environment.upper()
        environment_fit = 1.0 if environment == "ANY" or (environment == "INDOOR") == event.indoor else 0.25
        checks.append(environment_fit)

        accessibility_fit = 1.0 if not profile.accessibleOnly or event.accessible else 0.0
        checks.append(accessibility_fit)
        if profile.accessibleOnly and accessibility_fit == 1.0:
            reasons.append("Marked accessible-friendly in the dataset")

        date_fit = 0.55 if event.dateStatus in {"TENTATIVE_WINDOW", "DATES_TBA"} else 1.0
        checks.append(date_fit)
        return float(np.mean(checks)), reasons

    def _content_reasons(self, profile: UserProfile, event: EventDocument, similarity: float) -> list[str]:
        haystack = self._event_document(event).lower()
        selected_terms = dict.fromkeys(
            term.lower()
            for interest in profile.interests
            for term in interest.split()
            if len(term) > 2
        )
        matches = [term for term in selected_terms if term in haystack]
        if matches:
            return ["Content model matched: " + ", ".join(matches[:3])]
        if similarity > 0:
            return ["Content model found related words in the event description"]
        return ["Constraint fit, rather than text similarity, supports this option"]

    def _feature_row(self, profile: UserProfile, event: EventDocument, content: float, constraints: float) -> dict[str, Any]:
        row: dict[str, Any] = {
            "category": event.category.lower(),
            "companions": profile.companions.lower(),
            "environment": profile.environment.lower(),
            "crowd_tolerance": profile.crowdTolerance.lower(),
            "crowd_risk": event.crowdRisk.lower(),
            "content_similarity": content,
            "constraint_fit": constraints,
            "budget_ratio": min(2.0, profile.maxBudget / max(event.price, 1)) if not event.freeEntry else 2.0,
            "free_entry": float(event.freeEntry),
            "indoor": float(event.indoor),
            "accessible": float(event.accessible),
        }
        for interest in profile.interests:
            row[f"interest={interest.lower()}"] = 1.0
        return row

    def _learned_probability(self, profile: UserProfile, event: EventDocument, content: float, constraints: float) -> float | None:
        if self._preference_model is None:
            return None
        features = self._feature_row(profile, event, content, constraints)
        return float(self._preference_model.predict_proba([features])[0][1])

    def _read_feedback(self) -> list[dict[str, Any]]:
        if not self.feedback_file.exists():
            return []
        rows: list[dict[str, Any]] = []
        for line in self.feedback_file.read_text(encoding="utf-8").splitlines():
            try:
                rows.append(json.loads(line))
            except json.JSONDecodeError:
                continue
        return rows

    def _train_preference_model(self) -> bool:
        records = self._read_feedback()
        labels = [int(record["label"]) for record in records]
        if len(records) < self.minimum_feedback or len(set(labels)) < 2:
            return False

        rows = []
        for record in records:
            profile = UserProfile.model_validate(record["profile"])
            event = EventDocument.model_validate(record["event"])
            constraint_score, _ = self._constraint_fit(profile, event)
            # Feedback training uses transparent structured features; the live
            # content score is recomputed during ranking.
            rows.append(self._feature_row(profile, event, 0.0, constraint_score))

        pipeline = Pipeline([
            ("vectorizer", DictVectorizer(sparse=True)),
            ("classifier", LogisticRegression(max_iter=1000, class_weight="balanced", random_state=42)),
        ])
        pipeline.fit(rows, labels)
        joblib.dump(pipeline, self.preference_model_file)
        self._preference_model = pipeline
        self._trained_at = datetime.now(timezone.utc)
        return True

    def _load_preference_model(self) -> Pipeline | None:
        if not self.preference_model_file.exists():
            return None
        try:
            model = joblib.load(self.preference_model_file)
            self._trained_at = datetime.fromtimestamp(self.preference_model_file.stat().st_mtime, timezone.utc)
            return model
        except Exception:
            return None
