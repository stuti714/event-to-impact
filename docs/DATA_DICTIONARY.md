# Dataset dictionary

| Field | Meaning |
|---|---|
| `title` | Public-facing event or annual-awareness record |
| `category` | Career, Civic, Education, Environment, Health, Inclusion, Livelihood, National, Safety or Technology |
| `description` | Neutral summary of the event's public purpose |
| `startTime`, `endTime` | Fixed annual date or month boundary represented as application timestamps |
| `dateStatus` | `FIXED_ANNUAL_DATE` in the current nationwide dataset; legacy statuses remain supported by the API |
| `programme_status` / `verificationStatus` | `LOCAL_PROGRAMMES_VARY` for 2026 or `DETAILS_TBA` for 2027 |
| `area`, `city` | `Nationwide` and `India`; not a claim of one physical venue |
| `participationMode` | Typical channels such as online, campus, workplace, community or public institution |
| `audience` | People who may benefit from the topic; not an eligibility guarantee |
| `impactGoal` | One useful learning, awareness or participation outcome |
| `price`, `freeEntry` | Zero/free in the planning dataset; local organisers can set separate terms |
| `popularityScore` | Curated 1–10 public-activity band used only for comparison |
| `capacity`, `expectedAttendance` | Reserved for documented organiser data; zero in all seed records |
| `attendanceBasis` | `NO_LIVE_COUNT` for the seed dataset |
| suitability flags | Family, student, senior and accessibility planning cues |
| `sourceName`, `sourceUrl` | Provenance displayed directly to the user |
| `verifiedAt` | Last manual source-review timestamp |
| `verificationStatus` | Distinguishes verified annual dates from unconfirmed local programme details |

The CSV at `data/events-2026-2027.csv` is the human-readable snapshot. The Spring seed contains the additional UI and scoring fields.
