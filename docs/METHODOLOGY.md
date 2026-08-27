# Event to Impact methodology

## Public-activity score

Event to Impact produces a **comparative public-activity score**, not a live people count and not a calibrated attendance probability. It answers only: “Which listed annual activities may attract broader participation, and which visible signals caused that comparison?”

The rules layer starts from a curated 1–10 public-interest band and applies visible modifiers:

- free public participation: +8 points;
- public-holiday timing: +10 points;
- Saturday or Sunday: +7 points;
- outdoor evening activity: +4 points;
- an online option: −12 points because attendance is not tied to one venue.

The capped score maps to Low (0–42), Moderate (43–71) or High (72–100). Seeded capacity and expected attendance remain zero. If a future organiser supplies both values with a documented basis, the base may use `expectedAttendance / capacity`.

## Programme confidence

- **Medium:** the annual date is source-backed, but local programme and attendance ground truth are unavailable.
- **Low:** a date window is tentative, dates are TBA or 2027 local programme details are not yet announced.
- **High:** reserved for future organiser-provided attendance evidence with a documented basis.

## ML recommendation ranking

The content model uses `TfidfVectorizer` with unigrams and bigrams. It learns vocabulary from event titles, categories, descriptions, tags, audiences, impact goals and participation modes. Cosine similarity compares those vectors with the user's selected public-impact interests.

The practical-fit layer separately evaluates:

1. public-activity preference;
2. companion suitability;
3. online/indoor/outdoor preference;
4. accessibility preference;
5. event date confidence;
6. free participation.

Cold-start score = 70% content similarity + 30% practical fit.

After at least ten feedback records containing both positive and negative labels:

Learned score = 55% content similarity + 25% practical fit + 20% logistic-regression preference probability.

Every response includes score components and human-readable reasons. If FastAPI is unavailable, Spring Boot returns its deterministic rules ranking and labels it `RULE_FALLBACK`.

## Data freshness

Every record stores a source, URL, review timestamp, annual-date status and programme-verification status. A source-backed annual observance is not presented as a confirmed local venue. All 2027 local programme details require a source recheck before participation.
