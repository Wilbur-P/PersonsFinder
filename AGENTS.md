# Goal
Deliver a small but polished backend take-home project for the Persons Finder challenge.

# Constraints
- Time box: 2 hours
- Code must run locally with minimal setup
- Prefer small complete functionality over broad unfinished functionality

# Technical direction
- Use Spring Boot with the existing Gradle Kotlin DSL setup
- Prefer H2 for the challenge
- Keep architecture simple: controller / service / repository / dto / exception
- Implement:
  - POST /persons
  - PUT /persons/{id}/location
  - GET /persons/nearby
- Add AI_LOG.md and SECURITY.md
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
- Flag missing validation, weak docs, or unverified run instructions
