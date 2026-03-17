# Unfixed Issues

Current unfixed issues in this worktree, prioritized for submission readiness.

## Must fix before submission

1. Rate limiting can be spoofed via `X-Forwarded-For`.
- Evidence: `src/main/kotlin/com/persons/finder/config/WebMvcConfig.kt` reads client IP from header directly.
- Fix: trust forwarded headers only behind trusted proxy config, otherwise use `remoteAddr`.

2. Rate-limit key uses raw request URI and can be bypassed by changing path IDs.
- Evidence: `src/main/kotlin/com/persons/finder/service/RateLimiterService.kt` uses `"$endpoint|$clientId"`.
- Fix: key by normalized route template + method (example: `PUT:/persons/{id}/location`).

3. Prompt injection checks are heuristic and not broadly tested.
- Evidence: limited regex patterns and narrow negative tests.
- Fix: add stronger normalization (unicode/invisible chars), expand attack cases, and add tests for bypass attempts.

4. README run instructions are incomplete for reproducibility.
- Current gap: no explicit Java/JAVA_HOME prerequisite and no expected verification output.
- Fix: add prerequisites, exact commands, and expected success checks.

## Optional improvements

5. Validation cap duplication can drift.
- Evidence: controller hardcodes `limit <= 200`, service uses configurable `app.nearby.max-limit`.
- Fix: centralize limit validation in one place.

6. `name` field is reflected to API clients with minimal sanitization.
- Risk: downstream XSS if a client renders without escaping.
- Fix: apply stricter input policy for `name` or document output-encoding responsibility clearly.

7. Benchmark remains disabled and results are undocumented.
- Evidence: benchmark test is `@Disabled`; README has no p50/p95 data.
- Fix: run benchmark and add machine specs + measured results to README.

8. SECURITY claims are not explicitly mapped to implementation/tests.
- Fix: add a short traceability section in `SECURITY.md` (control -> class -> test).
