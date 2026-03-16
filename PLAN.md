# PLAN: Persons Finder Take-Home

## 1) Chosen scope
- Deliver a runnable Spring Boot API with the 3 required endpoints only:
  - `POST /persons`
  - `PUT /persons/{id}/location`
  - `GET /persons/nearby`
- Use H2 in-memory DB and Spring Data JPA.
- Keep architecture simple and explicit: `controller / service / repository / dto / exception`.
- Add `AI_LOG.md` and `SECURITY.md`.
- Add minimal Swagger/OpenAPI if dependency wiring is quick and low-risk.
- Add a benchmark test scenario for nearby search with 1 million seeded records.
- Add minimal API hardening: request validation limits, nearby radius cap, generic error responses, and safe logging.
- Add lightweight in-memory rate limiting for public endpoints.
- Optimize `GET /persons/nearby` to avoid full-table scans on large datasets.
- Prioritize completeness and correctness over extra features.

## 2) Assumptions
- Runtime is Java 11 with the existing Gradle wrapper.
- Person IDs are app-generated ULIDs (26-char string), not DB-generated numeric IDs.
- Only current location per person is needed (no location history).
- Nearby search radius is in kilometres.
- Nearby results are bounded with `limit` (default and max) to keep response/query time predictable.
- No authentication/authorization is implemented in challenge scope; this is a documented residual risk.
- Person name/job title/hobbies are plain user input with server-side validation.
- H2 in-memory persistence is acceptable for local demo/testing.

## 3) Package/file structure
- Keep base package: `com.persons.finder`.
- Implemented structure:
  - `controller/PersonController.kt`
  - `dto/request/CreatePersonRequest.kt`
  - `dto/request/UpdateLocationRequest.kt`
  - `dto/response/PersonCreatedResponse.kt`
  - `dto/response/LocationUpdatedResponse.kt`
  - `dto/response/NearbyPersonResponse.kt`
  - `dto/response/ErrorResponse.kt`
  - `entity/PersonEntity.kt`
  - `repository/PersonRepository.kt`
  - `service/PersonService.kt`
  - `service/AiBioService.kt`
  - `service/PromptSafetyService.kt`
  - `service/RateLimiterService.kt`
  - `service/impl/PersonServiceImpl.kt`
  - `service/impl/DeterministicMockAiBioService.kt`
  - `service/DistanceCalculator.kt`
  - `util/UlidGenerator.kt`
  - `config/WebMvcConfig.kt`
  - `exception/InvalidInputException.kt`
  - `exception/PersonNotFoundException.kt`
  - `exception/RateLimitExceededException.kt`
  - `exception/GlobalExceptionHandler.kt`
  - `resources/application.properties`
  - `resources/application-dev.properties`
  - `AI_LOG.md`
  - `SECURITY.md`

## 4) Data model
- Single `persons` table (simple and sufficient for scope):
  - `id` (PK, `VARCHAR(26)` ULID, generated in application service)
  - `name` (string, required)
  - `job_title` (string, required)
  - `hobbies_csv` (string, required; comma-separated sanitized hobbies)
  - `bio` (string, required)
  - `latitude` (double, required)
  - `longitude` (double, required)
  - `created_at` (timestamp)
  - `updated_at` (timestamp)
- Indexes:
  - Composite B-tree index on `(latitude, longitude)` for bounding-box prefiltering.
  - Optional single-column index on `updated_at` only if needed for future recency queries.
- Nearby distance is computed at query time with Haversine in service layer.

## 5) Endpoint contract
- `POST /persons`
  - Request body: `name`, `jobTitle`, `hobbies[]`, `location.latitude`, `location.longitude`
  - Validation: non-blank fields, length bounds, request size bounds, hobby count bounds, lat/lon ranges, hobbies non-empty.
  - Response: `201 Created` with `{ id, bio }`.
  - Errors: `400` validation/unsafe input.

- `PUT /persons/{id}/location`
  - Path var: `id` as ULID string.
  - Request body: `latitude`, `longitude`
  - Validation: ULID format + lat/lon ranges.
  - Response: `200 OK` (updated location summary).
  - Errors: `404` when person not found, `400` validation.

- `GET /persons/nearby?latitude=&longitude=&radiusKm=&limit=`
  - Validation: required query params, `radiusKm > 0`, server-side max radius cap, and `limit` within allowed bounds.
  - Response: `200 OK` list of nearby persons sorted by ascending distance and truncated to `limit`.
  - Item fields: `id`, `name`, `jobTitle`, `bio`, `latitude`, `longitude`, `distanceKm`.
  - Errors: `400` validation.

## 6) AI bio generation approach
- Define `AiBioService` interface to isolate bio generation.
- Use `DeterministicMockAiBioService` (no external LLM call):
  - Input: sanitized `jobTitle` + sanitized `hobbies` only.
  - Output: deterministic short quirky bio template based on stable input (same input -> same bio).
- Do not send `name` or `location` to AI service to reduce privacy risk.

## 7) Prompt injection mitigation approach
- Add `PromptSafetyService` before calling `AiBioService`:
  - Normalize whitespace and trim input.
  - Enforce allowlist character policy for `jobTitle` and each hobby.
  - Reject suspicious instruction-like patterns (example: `ignore instructions`, `system:`, `assistant:`, code fences, control characters).
  - Enforce max lengths and max hobby count.
- On violation return clear `400` error with safe message.
- Do not send `name`/`location` to AI generation.

## 8) Additional API security controls
- Validation hardening:
  - ULID format validation for path IDs.
  - Strict numeric bounds for lat/lon and `radiusKm` max cap.
  - Request payload and field-length limits to reduce abuse risk.
- Abuse controls:
  - Basic in-memory rate limiting (per IP, per endpoint window) with `429 Too Many Requests`.
- Error handling:
  - Global exception handler returns generic safe messages (no stack traces/internal details).
- Data handling:
  - Avoid raw PII request-body logging; log only minimum operational metadata.
- Environment controls:
  - Disable H2 console by default.
  - Restrict Swagger/OpenAPI exposure to local/dev profile.
- Document controls, tradeoffs, and residual risks in `SECURITY.md`.

## 9) Nearby performance strategy
- Query plan for `GET /persons/nearby`:
  - Step 1: Convert radius to a latitude/longitude bounding box.
  - Step 2: Repository query prefilters candidates using `latitude BETWEEN ...` and `longitude BETWEEN ...` (index-assisted).
  - Step 3: Compute exact Haversine distance only for candidates.
  - Step 4: Keep only records within radius, sort by distance ascending, return top `limit`.
- Safeguards:
  - Cap `radiusKm` to a reasonable max (for example `<= 50` km in challenge mode).
  - Cap `limit` (for example max `200`, default `50`).
  - Reject invalid values with `400` instead of executing expensive queries.
- Note: goal is practical performance for challenge scale; advanced geo indexing remains out of scope.

## 10) Test plan
- Unit tests:
  - `DeterministicMockAiBioService` returns stable output for identical input.
  - `PromptSafetyService` accepts normal text and rejects malicious payloads.
  - Nearby search logic filters by radius and sorts by distance correctly.
- Integration tests (Spring Boot + MockMvc + H2):
  - `POST /persons` happy path + validation failure.
  - `PUT /persons/{id}/location` happy path + not found.
  - `PUT /persons/{id}/location` invalid ULID returns `400`.
  - `GET /persons/nearby` returns expected sorted results.
  - `GET /persons/nearby` radius above cap returns `400`.
  - `GET /persons/nearby` limit above cap returns `400`.
  - High request burst triggers rate limit `429`.
  - Error responses do not leak stack traces/internal class names.
- Benchmark test:
  - Seed 1,000,000 person records (batched inserts) with generated coordinates.
  - Run `GET /persons/nearby` equivalent service query from a fixed point/radius.
  - Capture elapsed time and matched row count; document results and machine specs in `README.md`.
  - Record p50/p95 over multiple runs and verify no full-scan behavior in query plan/log output.
  - Challenge acceptance target: nearby query remains responsive on local machine for bounded radius/limit settings.
  - Keep benchmark test isolated (e.g., dedicated test class/profile) so normal test runs stay fast.
- Verify README run steps by actually running build/tests locally.

## 11) Out-of-scope items
- Real external LLM integration and key management.
- Authentication and authorization.
- Advanced geospatial indexing engines and distributed geo search.
- Pagination and advanced filtering for nearby endpoint.
- Production observability hardening.
