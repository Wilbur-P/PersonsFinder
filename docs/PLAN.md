# Plan: Persons Finder Backend

## 1. Chosen Scope
- Deliver a runnable Spring Boot API with the three required endpoints only:
  - `POST /persons`
  - `PUT /persons/{id}/location`
  - `GET /persons/nearby`
- Use an H2 in-memory database and Spring Data JPA.
- Keep the architecture simple and explicit: `controller / service / repository / dto / exception`.
- Add `docs/SECURITY.md`.
- Add minimal Swagger/OpenAPI if dependency wiring is quick and low-risk.
- Add a benchmark test scenario for nearby search with 1 million seeded records.
- Add minimal API hardening: request-validation limits, nearby radius cap, generic error responses, and safe logging.
- Optimise `GET /persons/nearby` to avoid full-table scans on large datasets.
- Prioritise completeness and correctness over extra features.

## 2. Assumptions
- Runtime is Java 11 with the existing Gradle wrapper.
- Person IDs are application-generated ULIDs (26-character string), not database-generated numeric IDs.
- Only the current location per person is needed; there is no location history.
- Nearby search radius is in kilometres.
- Nearby results are bounded with `limit` (default and max) to keep response and query time predictable.
- No authentication or authorisation is implemented in the current scope; this is a documented residual risk.
- Person name, job title, and hobbies are plain user input with server-side validation.
- H2 in-memory persistence is acceptable for local demo and testing.

## 3. Package/File Structure
- Keep the base package: `com.persons.finder`.
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
  - `service/impl/PersonServiceImpl.kt`
  - `service/impl/DeterministicMockAiBioService.kt`
  - `service/DistanceCalculator.kt`
  - `util/UlidGenerator.kt`
  - `exception/InvalidInputException.kt`
  - `exception/PersonNotFoundException.kt`
  - `exception/GlobalExceptionHandler.kt`
  - `resources/application.properties`
  - `resources/application-dev.properties`
  - `docs/SECURITY.md`

## 4. Data Model
- Single `persons` table (simple and sufficient for scope):
  - `id` (primary key, `VARCHAR(26)` ULID, generated in the application service)
  - `name` (string, required)
  - `job_title` (string, required)
  - `hobbies_csv` (string, required; comma-separated sanitised hobbies)
  - `bio` (string, required)
  - `latitude` (double, required)
  - `longitude` (double, required)
  - `created_at` (timestamp)
  - `updated_at` (timestamp)
- Indexes:
  - Composite B-tree index on `(latitude, longitude)` for bounding-box prefiltering.
  - Optional single-column index on `updated_at` only if needed for future recency queries.
- Nearby distance is computed at query time with Haversine in the service layer.

## 5. Endpoint Contract
- `POST /persons`
  - Request body: `name`, `jobTitle`, `hobbies[]`, `location.latitude`, `location.longitude`
  - Validation: non-blank fields, length bounds, request size bounds, hobby count bounds, latitude/longitude ranges, hobbies non-empty.
  - Response: `201 Created` with `{ id, bio }`.
  - Errors: `400` for validation and unsafe input.

- `PUT /persons/{id}/location`
  - Path variable: `id` as a ULID string.
  - Request body: `latitude`, `longitude`
  - Validation: ULID format plus latitude and longitude ranges.
  - Response: `200 OK` with an updated location summary.
  - Errors: `404` when the person is not found, `400` for validation.

- `GET /persons/nearby?latitude=&longitude=&radiusKm=&limit=`
  - Validation: required query parameters, `radiusKm > 0`, server-side max radius cap, and `limit` within allowed bounds.
  - Response: `200 OK` list of nearby persons sorted by ascending distance and truncated to `limit`.
  - Item fields: `id`, `name`, `jobTitle`, `bio`, `latitude`, `longitude`, `distanceKm`.
  - Errors: `400` for validation.

## 6. AI Biography Generation Approach
- Define `AiBioService` to isolate biography generation.
- Use `DeterministicMockAiBioService` with no external LLM call:
  - Input: sanitised `jobTitle` plus sanitised `hobbies` only.
  - Output: a deterministic short quirky biography template based on stable input, so identical input yields identical output.
- Do not send `name` or `location` to the AI service, to reduce privacy risk.

## 7. Prompt-Injection Mitigation Approach
- Add `PromptSafetyService` before calling `AiBioService`:
  - Normalise whitespace and trim input.
  - Enforce an allow-list character policy for `jobTitle` and each hobby.
  - Reject suspicious instruction-like patterns such as `ignore instructions`, `system:`, `assistant:`, code fences, and control characters.
  - Enforce max lengths and max hobby count.
- On violation, return a clear `400` error with a safe message.
- Do not send `name` or `location` to AI generation.

## 8. Additional API Security Controls
- Validation hardening:
  - ULID format validation for path IDs.
  - Strict numeric bounds for latitude, longitude, and the `radiusKm` max cap.
  - Request payload and field-length limits to reduce abuse risk.
- Error handling:
  - Global exception handling returns generic safe messages without stack traces or internal details.
- Data handling:
  - Avoid raw PII request-body logging; log only the minimum operational metadata.
- Environment controls:
  - Disable the H2 console by default.
  - Restrict Swagger/OpenAPI exposure to the local `dev` profile.
- Document controls, trade-offs, and residual risks in `docs/SECURITY.md`.

## 9. Nearby Performance Strategy
- Query plan for `GET /persons/nearby`:
  - Step 1: Convert the radius to a latitude/longitude bounding box.
  - Step 2: Repository query prefilters candidates using `latitude BETWEEN ...` and `longitude BETWEEN ...` with index assistance.
  - Step 3: Compute exact Haversine distance only for candidates.
  - Step 4: Keep only records within the radius, sort by ascending distance, and return the top `limit`.
- Safeguards:
  - Cap `radiusKm` to a reasonable max, for example `<= 50` km in the reference configuration.
  - Cap `limit`, for example max `200`, default `50`.
  - Reject invalid values with `400` instead of running expensive queries.
- Note: the goal is practical performance for the current scale; advanced geo-indexing remains out of scope.

## 10. Test Plan
- Unit tests:
  - `DeterministicMockAiBioService` returns stable output for identical input.
  - `PromptSafetyService` accepts normal text and rejects malicious payloads.
  - Nearby search logic filters by radius and sorts by distance correctly.
- Integration tests with Spring Boot, MockMvc, and H2:
  - `POST /persons` happy path plus validation failure.
  - `PUT /persons/{id}/location` happy path plus not found.
  - `PUT /persons/{id}/location` invalid ULID returns `400`.
  - `GET /persons/nearby` returns expected sorted results.
  - `GET /persons/nearby` radius above cap returns `400`.
  - `GET /persons/nearby` limit above cap returns `400`.
  - Error responses do not leak stack traces or internal class names.
- Benchmark test:
  - Seed 1,000,000 person records with batched inserts and generated coordinates.
  - Run the `GET /persons/nearby` equivalent service query from a fixed point and radius.
  - Capture elapsed time and matched row count; document results and machine specifications in `docs/README.md`.
  - Record p50 and p95 over multiple runs and verify there is no full-scan behaviour in query plan or logs.
  - Keep the benchmark test isolated so normal test runs remain fast.
- Verify the documented run steps by actually running build and test locally.

## 11. Out-of-Scope Items
- Real external LLM integration and key management.
- Authentication and authorisation.
- Advanced geospatial indexing engines and distributed geo search.
- Pagination and advanced filtering for the nearby endpoint.
- Production observability hardening.
