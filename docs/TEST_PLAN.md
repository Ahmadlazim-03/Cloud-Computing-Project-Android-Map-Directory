# Test Plan

## Objective
Validate the functional and non‑functional requirements of the Android Map Directory project.

## Scope
- API endpoints (`/categories`, `/places`, `/places/{id}`)
- Data integrity (15‑30 valid places displayed as markers)
- GPS permission handling and location reading
- Routing feature from place detail to map
- Connectivity handling (server down, no internet, empty response)

## Test Environment
- Android device (API 26+) or emulator
- Backend deployed on Vercel (or local dev server)
- Supabase PostgreSQL database seeded with 25 places
- Network conditions: Wi‑Fi, 4G, airplane mode

## Test Schedule
| Phase | Dates | Owner |
|-------|-------|-------|
| Preparation | 2026‑06‑07 to 2026‑06‑08 | QA Lead |
| Execution | 2026‑06‑09 to 2026‑06‑12 | QA Team |
| Reporting | 2026‑06‑13 | QA Lead |

## Acceptance Criteria
- All API responses return correct JSON schema and HTTP 200.
- At least 15 markers appear on the map with valid coordinates.
- App gracefully handles GPS denial and shows fallback UI.
- Routing opens external map intent with correct coordinates.
- User sees clear error messages when server is unreachable.
