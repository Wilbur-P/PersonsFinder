# AI_LOG

### 1. Mandatory AI Usage
Used AI tools to accelerate implementation and testing. Key interactions:

- I asked AI to use random ULID instead of DB-generated IDs. We switched to app-generated ULIDs, validated ULID path params, and returned ULID IDs in API responses.
- I asked AI to make sure there are no performance issues on `GET /persons/nearby`. We implemented bounding-box prefiltering, exact Haversine filtering, radius/limit caps, and a 1M-record benchmark scenario.
- I asked AI to ensure sanitization happens before calling the deterministic mock. We run `PromptSafetyService.sanitizeForBio(...)` before `DeterministicMockAiBioService.generateBio(...)`.
