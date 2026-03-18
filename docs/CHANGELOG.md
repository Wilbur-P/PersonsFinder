# Changelog

All notable changes to this project should be documented in this file.

The format is based on Keep a Changelog and this project follows Semantic Versioning once tagged releases begin.

## [Unreleased]

### Added
- Structured AI biography input using `BioGenerationInput`.
- Prompt-safety checks and AI/privacy documentation.
- Benchmark task for nearby search.

### Changed
- Validation now returns clearer field-level errors for missing `location`, `latitude`, and `longitude`.
- Rate limiting is documented as an infrastructure responsibility rather than implemented in-app.

### Repository
- Added CI workflow, contribution guide, templates, Docker support, and a consolidated `docs/` directory.
