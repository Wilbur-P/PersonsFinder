# Persons Finder Backend

Spring Boot (Kotlin) implementation of the Persons Finder challenge.

## Implemented Endpoints
- `POST /persons`
- `PUT /persons/{id}/location`
- `GET /persons/nearby`

## Tech Stack
- Java 11
- Spring Boot 2.7
- Kotlin + Gradle Kotlin DSL
- H2 in-memory DB
- Spring Data JPA
- Bean Validation

## Architecture
Simple layered structure:
- `controller`
- `service`
- `repository`
- `dto`
- `exception`

AI generation is isolated behind `AiBioService`, with deterministic mock implementation.

## Run Locally
```bash
./gradlew bootRun
```
Service starts at `http://localhost:8080`.

## API Docs (Swagger/OpenAPI)
Swagger is disabled by default. Run with dev profile:
```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```
Then open:
- `http://localhost:8080/swagger-ui/index.html`

## Example Requests
Create a person:
```bash
curl -X POST http://localhost:8080/persons \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Alice",
    "jobTitle": "Backend Engineer",
    "hobbies": ["Hiking", "Chess"],
    "location": {"latitude": 37.7749, "longitude": -122.4194}
  }'
```

Update location:
```bash
curl -X PUT http://localhost:8080/persons/{id}/location \
  -H 'Content-Type: application/json' \
  -d '{"latitude": 37.78, "longitude": -122.42}'
```

Nearby search:
```bash
curl 'http://localhost:8080/persons/nearby?latitude=37.7749&longitude=-122.4194&radiusKm=10&limit=20'
```

## Test
```bash
./gradlew test
```

## Benchmark Scenario
A benchmark test scenario for 1,000,000 records is included and intentionally disabled:
- `src/test/kotlin/com/persons/finder/benchmark/NearbyBenchmarkTest.kt`

Run it manually by removing `@Disabled` and executing:
```bash
./gradlew test --tests com.persons.finder.benchmark.NearbyBenchmarkTest
```

## Security and AI Notes
- `AI_LOG.md`
- `SECURITY.md`
