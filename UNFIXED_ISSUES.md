# Issue Status

All previously listed issues in this worktree have been addressed.

## Resolved

1. Forwarded-header spoofing in rate limiting.
- Fix: `WebMvcConfig` now trusts `X-Forwarded-For` only when explicitly enabled and only when `remoteAddr` is in configured trusted proxies.

2. Rate-limit bypass via raw URI path IDs.
- Fix: rate limit key now uses `METHOD + route template` (for example `PUT:/persons/{id}/location`) instead of raw request URI.

3. Prompt injection heuristics and bypass coverage.
- Fix: `PromptSafetyService` now performs NFKC normalization, strips control/invisible chars, detects obfuscated prompt payloads, and has expanded unit tests.

4. README reproducibility gaps.
- Fix: added prerequisites (`java`, `JAVA_HOME`), exact run/test commands, and expected success/log checks.

5. Validation-cap duplication drift.
- Fix: centralized nearby `limit` cap enforcement in `PersonServiceImpl`; removed controller hardcoded limit cap annotations.

6. Unsafe `name` reflection risk.
- Fix: added strict server-side `name` sanitization policy in `PersonServiceImpl` and integration test coverage.

7. Benchmark disabled and undocumented.
- Fix: benchmark is now isolated as `benchmarkTest`, runnable explicitly, and README includes captured p50/p95 + machine specs.

8. SECURITY traceability gap.
- Fix: added `Control Traceability` section mapping controls to implementation and tests.
