# Event to Impact ML service

FastAPI ranks candidate events supplied by Spring Boot. Spring remains the source of truth for the calendar and feedback.

## Ranking stages

1. **Content cold start:** TF-IDF learns one- and two-word features from title, purpose, audience, impact, participation mode and tags. Cosine similarity compares events with the user's selected topics.
2. **Practical fit:** public-activity preference, companions, setting, accessibility, date confidence and free participation remain separately visible.
3. **Feedback learning:** after at least ten real records containing both positive and negative labels, logistic regression adds a preference probability.

- cold start: 70% content similarity + 30% practical fit;
- learned mode: 55% content similarity + 25% practical fit + 20% learned preference.

These are relative ranking scores, not attendance or benefit probabilities.

## Run

From the project root:

```powershell
.\setup-ml.cmd
.\start-ml.cmd
```

The service runs at `http://127.0.0.1:8001`; FastAPI documentation is available at `/docs`.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/health` | Service and model-mode status |
| POST | `/recommend` | Rank supplied events for a profile |
| POST | `/feedback` | Save one preference label and retrain when eligible |
| GET | `/model-card` | Return algorithm, vocabulary, feedback and limitations |
