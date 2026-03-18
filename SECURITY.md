# SECURITY

## How inputs are sanitized before sending to the AI service
Bio generation is behind `AiBioService` and currently uses a deterministic mock. If a live model is enabled later, sanitization happens before that service call. The service contract uses structured `BioGenerationInput` rather than prompt concatenation.

Controls:
- Data minimization: only `jobTitle` and `hobbies` are used for bio generation.
- PII exclusion: `name` and `location` are not sent to the AI service.
- Structured input boundary: sanitized fields are passed as typed fields, which avoids delimiter collisions and prompt-string assembly mistakes.
- Unicode normalization: NFKC normalization plus control/invisible character stripping.
- Validation: enforce max lengths and hobby-count limits.
- Character allowlist: accept only expected safe characters for `jobTitle` and hobbies.
- Prompt-injection checks: reject instruction-like and obfuscated payloads.
- Failure behavior: return `400 Bad Request` with a safe message.

## Privacy risks of sending PII (name/location) to third-party models
- Retention risk: prompts/responses may be stored by the provider.
- Re-identification risk: name + location can directly identify individuals.
- Compliance risk: cross-border processing, subprocessors, and data residency issues.
- Breach/supply-chain risk: third-party compromise exposes customer data.
- Over-collection risk: unnecessary PII increases incident impact.

## High-security banking architecture approach
For a banking-grade system, treat external LLMs as untrusted for raw customer PII.

- Keep raw PII inside bank-controlled systems.
- Introduce a privacy gateway for redaction/tokenization and policy enforcement.
- Prefer private model hosting with no-training/no-retention guarantees.
- Encrypt in transit and at rest (customer-managed keys where possible).
- Enforce least privilege, audited access, and continuous monitoring.
- Apply DLP/egress controls and vendor legal controls (DPA, residency, subprocessors).
- Run recurring prompt-injection/data-exfiltration security tests.
- Enforce rate limiting and abusive-traffic controls at API gateway, load balancer, ingress, or WAF layers rather than inside this app.

## Control Traceability
| Control | Implementation | Verification |
|---|---|---|
| Prompt input normalization + injection filtering | `PromptSafetyService` | `PromptSafetyServiceTest` |
| AI data minimization + structured AI input | `PersonServiceImpl#createPerson` -> `PromptSafetyService.sanitizeForBio(...)` -> `AiBioService.generateBio(BioGenerationInput)` | Code-path verification + service tests + integration flow in `PersonControllerIntegrationTest` |
| Strict name policy to reduce unsafe reflected content | `PersonServiceImpl#sanitizeName` | `PersonControllerIntegrationTest` (`POST persons rejects unsafe name values`) |
| Infrastructure-managed rate limiting | API gateway / load balancer / ingress / WAF outside the app | Documented limitation and deployment guidance |
| Generic error responses (no stack traces) | `GlobalExceptionHandler` | `PersonControllerIntegrationTest` (`error response does not leak stack traces or class names`) |
