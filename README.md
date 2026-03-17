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
- H2
- Spring Data JPA
- Bean Validation

## Architecture
Simple layered structure:
- `controller`
- `service`
- `repository`
- `dto`
- `exception`

AI generation is isolated behind `AiBioService` with deterministic mock implementation.

## Prerequisites
- Java 11 installed
- `JAVA_HOME` pointing to Java 11

Verify environment:
```bash
java -version
./gradlew --version
```
Expected checks:
- `java -version` shows Java 11.
- `./gradlew --version` shows Gradle `7.6.1`.

## Run Locally
```bash
./gradlew bootRun
```
Service base URL: `http://localhost:8080`

Expected startup confirmation in logs:
- `Tomcat started on port(s): 8080 (http)`
- `Started ApplicationStarter`

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
Run the regular suite (benchmark excluded):
```bash
./gradlew test
```
Expected result:
- `BUILD SUCCESSFUL`

## Benchmark
Benchmark is isolated from normal tests and runs via dedicated task:
```bash
./gradlew benchmarkTest
```

Recent local benchmark run (`2026-03-17`):
- Seed size: `1,000,000` records
- Seed time: `19533ms`
- Nearby timings: `[73, 1, 1, 1, 0]` ms
- p50: `1ms`
- p95: `1ms`

Machine specs for the run:
- OS: `Linux 6.6.87.2-microsoft-standard-WSL2 x86_64`
- CPU: `Intel(R) Core(TM) Ultra 5 225H` (`14` cores)
- Java: `OpenJDK 11.0.30`

Note: numbers are environment-dependent and should be treated as local reference values.

## Known Limitations
- No authentication/authorization is implemented (challenge scope).
- Rate limiting is in-memory and suitable for a single-node setup only.
- Prompt-injection protection is heuristic-based and not a complete guarantee.
- Benchmark results are local reference values, not CI performance gates.

## Security and AI Notes
- `AI_LOG.md`
- `SECURITY.md`
