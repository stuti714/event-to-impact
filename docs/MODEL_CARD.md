# Model card — Event to Impact recommendation engine

## Intended use

Rank source-backed public-interest events across India according to a user's selected topics and practical preferences. The model is a 2026–2027 discovery aid; it is not a crowd counter, attendance forecast, safety system or eligibility decision.

## Model design

| Layer | Technique | Purpose |
|---|---|---|
| Content | TF-IDF unigrams and bigrams | Learns useful vocabulary from purpose, audience, impact and tags |
| Similarity | Cosine similarity | Measures alignment with selected interests |
| Practical fit | Transparent rules | Keeps companions, accessibility, setting and activity preference auditable |
| Preference learning | Logistic regression | Learns from explicit Interested, Saved and Not-for-me labels after cold start |

## Modes

- `CONTENT_BASED`: normal cold-start mode; no supervised accuracy is claimed.
- `HYBRID_LEARNED`: activates after at least 10 feedback samples and both label classes exist.
- `RULE_FALLBACK`: deterministic Spring ranking when the Python service is offline.

## Inputs

- event title, category, description, tags, audience, impact goal and participation mode;
- user-selected interests, companions, activity comfort, environment and accessibility preference;
- free-participation flag, suitability flags, date status and rule-based public-activity band.

No camera stream, face data, location history or sensitive demographic profile is used.

## Output

The service returns a relative ranking score with content relevance, practical fit and—after learning—preference probability. None of these values is an attendance prediction or guaranteed benefit.

## Evaluation policy

Deterministic tests verify that topic-relevant events outrank unrelated events. Runtime catalog coverage is reported. Accuracy, precision and F1 are intentionally withheld until enough real labeled interactions exist for a held-out evaluation.

## Limitations

1. The 44 records represent 22 recurring annual observances, not the full Indian event ecosystem.
2. Text quality, source coverage and tags influence similarity.
3. Early user feedback may be sparse or imbalanced.
4. Feedback represents preference, not event quality, attendance, safety or social impact.
5. Public-activity context remains a rules heuristic until documented attendance ground truth exists.

## Reproducibility

- logistic-regression random state: `42`;
- model version and weights: `ml_service/recommender.py`;
- tests: `ml_service/test_recommender.py`;
- runtime feedback and generated models are excluded from Git.
