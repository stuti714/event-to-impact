from fastapi import FastAPI

from .recommender import RecommendationEngine
from .schemas import FeedbackRequest, FeedbackResponse, ModelCard, RecommendationRequest, RecommendationResponse

app = FastAPI(
    title="Event to Impact Recommendation Service",
    version="1.0.0",
    description="Content-based event ranking with an optional feedback-trained preference layer.",
)
engine = RecommendationEngine()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP", "service": "Event to Impact ML", "mode": engine.mode}


@app.post("/recommend", response_model=RecommendationResponse)
def recommend(request: RecommendationRequest) -> RecommendationResponse:
    return engine.recommend(request)


@app.post("/feedback", response_model=FeedbackResponse)
def feedback(request: FeedbackRequest) -> FeedbackResponse:
    return engine.record_feedback(request)


@app.get("/model-card", response_model=ModelCard)
def model_card() -> ModelCard:
    return engine.model_card()
