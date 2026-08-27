from pathlib import Path

from ml_service.recommender import RecommendationEngine
from ml_service.schemas import EventDocument, FeedbackRequest, RecommendationRequest, UserProfile


def event(event_id: int, title: str, category: str, tags: str, price: int, risk: str) -> EventDocument:
    return EventDocument(
        id=event_id,
        title=title,
        category=category,
        description=f"An India-wide {category.lower()} awareness programme",
        tags=tags,
        venue="Verified programmes announced by local organisers",
        area="Nationwide",
        audience="Students, families and citizens",
        participationMode="Online + campus + community",
        impactGoal=f"Take one useful action related to {category.lower()}",
        price=price,
        freeEntry=price == 0,
        studentFriendly=True,
        accessible=True,
        dateStatus="DATE_VERIFIED",
        crowdRisk=risk,
    )


def test_content_model_ranks_relevant_event_first(tmp_path: Path) -> None:
    engine = RecommendationEngine(tmp_path)
    profile = UserProfile(
        interests=["career", "skills"], maxBudget=0,
        crowdTolerance="MODERATE", companions="STUDENTS",
    )
    skills = event(1, "World Youth Skills Day", "Career", "career,skills,students", 0, "LOW")
    health = event(2, "World Health Day", "Health", "health,wellbeing", 0, "HIGH")

    result = engine.recommend(RecommendationRequest(profile=profile, events=[health, skills]))

    assert result.modelMode == "CONTENT_BASED"
    assert result.scores[0].eventId == skills.id
    assert result.scores[0].contentScore > result.scores[1].contentScore


def test_real_feedback_activates_logistic_regression_only_after_threshold(tmp_path: Path) -> None:
    engine = RecommendationEngine(tmp_path, minimum_feedback=4)
    profile = UserProfile(interests=["education", "science"], maxBudget=0)
    science = event(1, "National Science Day", "Education", "science,education", 0, "MODERATE")
    unrelated = event(2, "World Water Day", "Environment", "water,environment", 0, "MODERATE")

    actions = [("SAVED", science), ("INTERESTED", science), ("NOT_FOR_ME", unrelated), ("NOT_FOR_ME", unrelated)]
    responses = [engine.record_feedback(FeedbackRequest(action=action, profile=profile, event=item)) for action, item in actions]

    assert responses[1].modelMode == "CONTENT_BASED"
    assert responses[-1].modelMode == "HYBRID_LEARNED"
    result = engine.recommend(RecommendationRequest(profile=profile, events=[science, unrelated]))
    assert result.modelMode == "HYBRID_LEARNED"
    assert all(score.learnedScore is not None for score in result.scores)
