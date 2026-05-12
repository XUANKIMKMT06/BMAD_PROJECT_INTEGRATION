---
stepsCompleted:
  - step-01-preflight
  - step-02-generate-pipeline
  - step-03-configure-quality-gates
  - step-04-validate-and-summary
lastStep: step-04-validate-and-summary
lastSaved: 2026-05-12
---

# CI Pipeline Progress

## Step 1: Preflight

- Git repository: present; remote `origin` → GitHub.
- Default branch: `main`.
- `test_stack_type`: `backend` (Maven `pom.xml`, Spring Boot, no browser E2E framework).
- `test_framework`: JUnit 5 + Spring Boot Test via Maven Surefire.
- Local verification: `user-mgmt/mvnw.cmd test` passed.
- `ci_platform`: `github-actions` (requested).
- Java runtime: 17 (`.java-version`, `pom.xml`).
- Existing workflow under `user-mgmt/.github/workflows/build.yml` is not at the repository root; new canonical workflow is `.github/workflows/test.yml`.

## Step 2: Generate Pipeline

- Output: `.github/workflows/test.yml`
- Stages: `lint` → `test` (2 Surefire shards) → `performance-smoke` (pull requests) → `performance-p0` (weekly schedule) → `report`
- Maven cache via `actions/setup-java@v4`
- Artifacts: Surefire reports on failure; k6 summaries under `_bmad-output/test-artifacts/performance/`
- Contract testing: not enabled (`tea_use_pactjs_utils: false`)

## Step 3: Quality Gates

- Burn-in: skipped (backend-only; targets UI flakiness).
- Gates: Maven `validate`/`compile` before tests; Surefire must pass on every shard; k6 smoke on pull requests; P0 scenarios on schedule.
- Notifications: GitHub Actions job summary in `report`; optional Slack/email not configured.
- Performance credentials use repository defaults from `user-mgmt/.env.example` unless overridden with repository variables.

## Step 4: Validate and Summarize

- Checklist: config at platform path, stages and sharding, artifacts, documented env defaults.
- Next steps: push to GitHub and confirm Actions runs; set repository variables if non-default k6 credentials are required; retire or align `user-mgmt/.github/workflows/build.yml` if it should not duplicate root workflows.
