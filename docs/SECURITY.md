# Security

## How Inputs Are Sanitised Before Sending To The AI Service
Biography generation sits behind `AiBioService` and currently uses a deterministic mock. If a live model is enabled later, sanitisation happens before that service call. The service contract uses structured `BioGenerationInput` rather than prompt concatenation.

Controls:
- Data minimisation: only `jobTitle` and `hobbies` are used for biography generation.
- PII exclusion: `name` and `location` are not sent to the AI service.
- Structured input boundary: sanitised fields are passed as typed fields, which avoids delimiter collisions and prompt-string assembly mistakes.
- Unicode normalisation: NFKC normalisation plus control and invisible character stripping.
- Validation: enforce maximum lengths and hobby-count limits.
- Character allow-list: accept only expected safe characters for `jobTitle` and hobbies.
- Prompt-injection checks: reject instruction-like and obfuscated payloads.
- Failure behaviour: return `400 Bad Request` with a safe message.

## Privacy Risks Of Sending PII To Third-Party Models
- Retention risk: prompts and responses may be stored by the provider.
- Re-identification risk: name plus location can directly identify individuals.
- Compliance risk: cross-border processing, subprocessors, and data residency issues.
- Breach and supply-chain risk: third-party compromise exposes customer data.
- Over-collection risk: unnecessary PII increases incident impact.

## Control Traceability
| Control | Implementation | Verification |
|---|---|---|
| Prompt input normalisation plus injection filtering | `PromptSafetyService` | `PromptSafetyServiceTest` |
| AI data minimisation plus structured AI input | `PersonServiceImpl#createPerson` -> `PromptSafetyService.sanitizeForBio(...)` -> `AiBioService.generateBio(BioGenerationInput)` | Code-path verification plus service tests and integration flow in `PersonControllerIntegrationTest` |
| Strict name policy to reduce unsafe reflected content | `PersonServiceImpl#sanitizeName` | `PersonControllerIntegrationTest` (`POST persons rejects unsafe name values`) |
| Infrastructure-managed rate limiting | API gateway, load balancer, ingress, or WAF outside the app | Documented limitation and deployment guidance |
| Generic error responses without stack traces | `GlobalExceptionHandler` | `PersonControllerIntegrationTest` (`error response does not leak stack traces or class names`) |
