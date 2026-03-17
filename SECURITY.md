# SECURITY

## How inputs are sanitized before sending to the LLM
For this challenge, bio generation is behind `AiBioService` and currently uses a deterministic mock. If a live LLM is enabled, sanitization happens before that service call:

- Data minimization: only `jobTitle` and `hobbies` are used for bio generation.
- PII exclusion: `name` and `location` are not sent to the AI service.
- Normalization: trim text and collapse repeated whitespace.
- Validation: enforce max lengths and hobby-count limits.
- Character allowlist: accept only expected safe characters for `jobTitle` and hobbies.
- Prompt-injection checks: reject instruction-like payloads (for example `ignore instructions`, `system:`, `assistant:`, code fences, and control characters).
- Failure behaviour: return `400 Bad Request` with a safe error message.

## Privacy risks of sending PII (name/location) to a third-party model
- Retention risk: prompts/responses may be stored by the provider.
- Re-identification risk: name + location can directly identify individuals.
- Compliance risk: cross-border processing, subprocessors, and data residency issues.
- Breach/supply-chain risk: third-party compromise exposes customer data.
- Over-collection risk: sending unnecessary PII increases impact if leaked.

## High-security banking architecture approach
For a banking-grade system, treat external LLMs as untrusted for raw customer PII:

- Keep raw PII inside bank-controlled systems; do not send raw name/location to public LLM APIs.
- Introduce a privacy gateway that redacts/tokenizes data and enforces policy before model calls.
- Prefer private model hosting (bank VPC/on-prem) with no-training/no-retention guarantees.
- Encrypt in transit and at rest (customer-managed keys where possible).
- Enforce least-privilege access, strong audit logging, and continuous monitoring.
- Apply DLP and egress controls; require vendor legal controls (DPA, residency, subprocessors).
- Regularly run prompt-injection and data-exfiltration security tests.
