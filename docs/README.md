# Persons Finder

[![CI](https://github.com/Wilbur-P/PersonsFinder/actions/workflows/ci.yml/badge.svg)](https://github.com/Wilbur-P/PersonsFinder/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)
[![Java 11](https://img.shields.io/badge/Java-11-blue.svg)](https://adoptium.net/)

Persons Finder is a small Spring Boot reference backend for storing people, updating their location, and finding nearby matches with deterministic AI-assisted biography generation.

It is intentionally compact: easy to run locally, easy to read, and opinionated about backend basics such as validation, error responses, privacy boundaries, and simple geospatial querying.

## Highlights
- `POST /persons` to create a person with a generated biography
- `PUT /persons/{id}/location` to update coordinates
- `GET /persons/nearby` for radius-based lookup
- Structured AI input via `BioGenerationInput`
- Validation and safe error responses
- Swagger/OpenAPI available in `dev`
- Dedicated benchmark task for nearby-search experiments

## Architecture
The project keeps a deliberately small layered design:
- `controller`
- `service`
- `repository`
- `dto`
- `exception`

Request flow:
1. The controller validates request shape and query parameters.
2. The service sanitises `jobTitle` and `hobbies` for AI usage.
3. `AiBioService` generates a deterministic mock biography without using `name` or `location`.
4. The repository persists the person record.
5. Nearby search uses a bounding-box prefilter plus exact Haversine distance filtering.

## Tech Stack
- Java 11
- Kotlin
- Spring Boot 2.7
- Gradle Kotlin DSL
- H2 for local development
- Spring Data JPA
- Bean Validation
- Springdoc OpenAPI UI

## Quick Start
Prerequisites:
- Java 11 installed
- `JAVA_HOME` pointing to Java 11

Verify the environment:
```bash
java -version
./gradlew --version
```

Run locally:
```bash
./gradlew bootRun
```

Service base URL: `http://localhost:8080`

Run tests:
```bash
./gradlew test
```

## Docker
Build the image:
```bash
docker build -t persons-finder .
```

Run the container:
```bash
docker run --rm -p 8080:8080 persons-finder
```

## API Docs
Swagger is disabled by default. Run with the dev profile:
```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Then open:
- `http://localhost:8080/swagger-ui/index.html`

## Example API Usage
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

Example response:
```json
{
  "id": "01JQ7RPYQ67V2N8S1GGF95K2RA",
  "bio": "Curious Backend Engineer who enjoys chess and hiking. Signature 5fd2ac."
}
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

Example response:
```json
[
  {
    "id": "01JQ7RPYQ67V2N8S1GGF95K2RA",
    "name": "Alice",
    "jobTitle": "Backend Engineer",
    "bio": "Curious Backend Engineer who enjoys chess and hiking. Signature 5fd2ac.",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "distanceKm": 0.0
  }
]
```

## Configuration Notes
- Swagger/OpenAPI is disabled by default and enabled in the `dev` profile.
- Nearby-search safeguards are configurable through:
  - `app.nearby.max-radius-km`
  - `app.nearby.default-limit`
  - `app.nearby.max-limit`
- H2 is used for local simplicity; move to PostgreSQL or another production database for real deployments.

## Benchmark
Benchmark tests are isolated from the normal suite:
```bash
./gradlew benchmarkTest
```

Recent local benchmark run (`2026-03-17`):
- Seed size: `1,000,000` records
- Seed time: `19533ms`
- Nearby timings: `[73, 1, 1, 1, 0]` ms
- p50: `1ms`
- p95: `1ms`

These numbers are local reference values, not release gates.

## Security And AI
- [`SECURITY.md`](./SECURITY.md)

Biography generation uses a typed `BioGenerationInput` object rather than prompt concatenation. `jobTitle` and `hobbies` are sanitised before entering the AI boundary, and `name` / `location` are intentionally excluded.

## Repository Guidance
- Contributions: see [`CONTRIBUTING.md`](./CONTRIBUTING.md)
- Change history: see [`CHANGELOG.md`](./CHANGELOG.md)
- Licence: MIT in [`../LICENSE`](../LICENSE)
- CI: GitHub Actions runs `./gradlew test` on pushes and pull requests

## Known Limitations
- No authentication or authorisation is implemented.
- No application-level rate limiting is implemented; production deployments should enforce throttling at the API gateway, load balancer, ingress, or WAF layer.
- Prompt-injection protection is heuristic-based, not a complete guarantee.
- H2 is fine for local/demo use but not the intended production database.
