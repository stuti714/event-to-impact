from __future__ import annotations

import csv
from collections import Counter
from pathlib import Path
from urllib.parse import urlparse

DATASET = Path(__file__).with_name("events-2026-2027.csv")
REQUIRED = {"year", "title", "category", "date_or_window", "date_status", "programme_status", "coverage", "participation_mode", "audience", "impact_goal", "price_inr", "source_name", "source_url"}
ALLOWED_STATUS = {"VERIFIED", "DATE_VERIFIED", "FIXED_ANNUAL_DATE", "CURATED_VISIT_DATE", "TENTATIVE_WINDOW", "DATES_TBA"}
ALLOWED_PROGRAMME_STATUS = {"LOCAL_PROGRAMMES_VARY", "DETAILS_TBA"}


def validate() -> dict[str, object]:
    with DATASET.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle)
        if not REQUIRED.issubset(reader.fieldnames or []):
            raise ValueError(f"Missing columns: {sorted(REQUIRED - set(reader.fieldnames or []))}")
        rows = list(reader)

    if len(rows) != 44:
        raise ValueError(f"Expected 44 records, found {len(rows)}")
    event_keys = [(row["year"], row["title"].strip().lower()) for row in rows]
    if len(event_keys) != len(set(event_keys)):
        raise ValueError("Duplicate event title found within the same year")
    for number, row in enumerate(rows, start=2):
        missing = [field for field in REQUIRED if not row[field].strip()]
        if missing:
            raise ValueError(f"Row {number} has blank fields: {missing}")
        if row["year"] not in {"2026", "2027"}:
            raise ValueError(f"Row {number} has unsupported year")
        if row["date_status"] not in ALLOWED_STATUS:
            raise ValueError(f"Row {number} has unsupported date status")
        if row["programme_status"] not in ALLOWED_PROGRAMME_STATUS:
            raise ValueError(f"Row {number} has unsupported programme status")
        expected_programme_status = "LOCAL_PROGRAMMES_VARY" if row["year"] == "2026" else "DETAILS_TBA"
        if row["programme_status"] != expected_programme_status:
            raise ValueError(f"Row {number} has a programme status inconsistent with its year")
        if urlparse(row["source_url"]).scheme not in {"http", "https"}:
            raise ValueError(f"Row {number} has an invalid source URL")
        if int(row["price_inr"]) < 0:
            raise ValueError(f"Row {number} has a negative price")

    return {
        "records": len(rows),
        "years": dict(sorted(Counter(row["year"] for row in rows).items())),
        "categories": len(set(row["category"] for row in rows)),
        "sources": len(set(row["source_url"] for row in rows)),
        "programme_recheck": sum(row["programme_status"] == "DETAILS_TBA" for row in rows),
    }


if __name__ == "__main__":
    profile = validate()
    print("Dataset validation: PASS")
    print(" | ".join(f"{key}={value}" for key, value in profile.items()))
