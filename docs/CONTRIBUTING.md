# Contributing

Thanks for contributing to Persons Finder.

## Development Setup
- Install Java 11 and ensure `JAVA_HOME` points to it.
- Verify the environment with `java -version` and `./gradlew --version`.
- Start the app with `./gradlew bootRun`.
- Run tests with `./gradlew test`.

## Expectations
- Keep the architecture simple: controller / service / repository / dto / exception.
- Prefer small, complete changes over broad unfinished changes.
- Preserve the AI privacy rules: do not send `name` or `location` to AI-related services.
- Keep validation and error responses explicit.
- Update [`README.md`](./README.md) or [`SECURITY.md`](./SECURITY.md) when behaviour or operating assumptions change.

## Pull Requests
- Write focused commits and pull requests.
- Include tests for behaviour changes.
- Update documentation for any endpoint, configuration, or workflow change.
- Call out trade-offs and limitations in the pull request description where relevant.

## Reporting Issues
- Use the GitHub issue templates where possible.
- Include reproduction steps, expected behaviour, and current behaviour.
