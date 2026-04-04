# Goal
Deliver a small but polished backend project for Persons Finder.

# Technical direction
- Use Spring Boot with the existing Gradle Kotlin DSL setup
- Prefer H2 for this reference implementation
- Keep architecture simple: controller / service / repository / dto / exception
- Implement:
  - POST /persons
  - PUT /persons/{id}/location
  - GET /persons/nearby
- Add `docs/SECURITY.md`
- Add Swagger/OpenAPI if quick to wire up

# AI architecture
- Isolate bio generation behind an AiBioService interface
- Prefer deterministic mock implementation over a live LLM call
- Do not send personal information to the AI service
- Add a prompt injection safeguard for jobTitle and hobbies

# Engineering rules
- Keep naming consistent
- Avoid over-engineering
- Add validation and clear error responses
- Add minimal but real tests
- Ensure README run steps are verified

# Review rules
- Flag anything that looks stitched together by AI
- Flag inconsistent naming and unnecessary abstractions
- Flag missing validation, weak documentation, or unverified run instructions
