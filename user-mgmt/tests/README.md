# User Management Test Framework

This project uses **JUnit 5** with **Spring Boot Test** and **MockMvc** for functional API and integration coverage, and **k6** for performance NFR validation per the user-mgmt performance epic.

## Setup

1. Install **JDK 17** (see `.java-version`).
2. Install **k6** v0.47+ on runners that execute performance scripts.
3. Copy `user-mgmt/.env.example` to `.env` when you need local URL or credential overrides.
4. From `user-mgmt`, run:

```powershell
.\mvnw.cmd test
```

Integration tests use the `test` profile via `src/test/resources/application-test.properties`.

## Running tests

| Command | Purpose |
| --- | --- |
| `.\mvnw.cmd test` | Run the full unit and integration suite |
| `.\mvnw.cmd -Dtest=ExampleAuthApiTest test` | Run one test class |
| `.\mvnw.cmd verify` | Run the full Maven lifecycle, including integration checks when configured |
| `.\tests\nfr\performance\scripts\run-smoke.ps1` | k6 smoke (P1) with summary JSON archived |
| `.\tests\nfr\performance\scripts\run-p0.ps1` | k6 P0 suite (pf01–pf04) |

## Architecture

### Functional (JUnit)

- `src/test/java/com/example/management/api/` — API-focused examples and future endpoint tests
- `src/test/java/com/example/management/support/fixtures/` — shared Spring Boot test bases
- `src/test/java/com/example/management/support/helpers/` — auth and HTTP helpers
- `src/test/java/com/example/management/support/factories/` — request builders with override-friendly defaults
- `src/test/resources/` — test profile configuration

### Performance (k6)

- `tests/nfr/performance/` — P0/P1 scenario scripts (`smoke.k6.js`, `pf01`–`pf04`)
- `tests/nfr/performance/lib/` — shared config, auth helpers, SLO thresholds, summary archival
- `tests/nfr/performance/gherkin/` — Gherkin traceability for performance scenarios
- `tests/nfr/performance/scripts/` — PowerShell runners for smoke and P0 batches
- `_bmad-output/test-artifacts/performance/` — k6 `summary.json` output (override with `PERF_ARTIFACT_DIR`)

## Best practices

- Seed state through API helpers or factories, not through the browser UI.
- Keep tests independent; use unique usernames from factories to avoid collisions.
- Prefer JSON-path assertions and explicit status checks over brittle string parsing.
- Reuse `BaseIntegrationTest` for new API and integration tests.
- Run performance jobs only with the Spring `perf` profile (`application-perf.properties`).

## CI notes

- Use JDK 17 on the runner.
- Run `mvn test` from `user-mgmt` on every commit.
- Run k6 smoke on PR when the app and k6 runner are available; defer full P0 to nightly.
- Set `BASE_URL`, `ADMIN_USER`, `ADMIN_PASSWORD`, and `USER_PASSWORD` for k6 jobs.
- Archive k6 summaries under `_bmad-output/test-artifacts/performance/`.

## Performance (k6)

Performance scripts are **not** part of `mvn test`.

1. Start the app with the `perf` profile (reduced logging):

```powershell
cd user-mgmt
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=perf"
```

2. In another shell, run smoke or P0 scenarios:

```powershell
cd user-mgmt
$env:BASE_URL = "http://localhost:8080"
.\tests\nfr\performance\scripts\run-smoke.ps1
.\tests\nfr\performance\scripts\run-p0.ps1
```

Manual k6 invocation:

```powershell
k6 run tests/nfr/performance/smoke.k6.js
k6 run tests/nfr/performance/pf01-admin-list.k6.js
```

Design thresholds and scenario catalog: `_bmad-output/test-artifacts/test-design/test-design-epic-user-mgmt-performance.md`. Gherkin TC specs: `tests/nfr/performance/gherkin/user-mgmt-performance.feature`.

## References

- TEA knowledge: data factories, fixture architecture, API testing patterns, NFR criteria
- Project context: `user-mgmt/project-context.md`
- Performance test design: `_bmad-output/test-artifacts/test-design/test-design-epic-user-mgmt-performance.md`
