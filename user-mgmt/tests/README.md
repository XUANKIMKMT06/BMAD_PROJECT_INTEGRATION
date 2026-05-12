# User Management Test Framework

This project uses **JUnit 5** with **Spring Boot Test** and **MockMvc** for API and integration coverage.

## Setup

1. Install **JDK 17**.
2. Copy `user-mgmt/.env.example` to `.env` if you need local URL overrides.
3. From `user-mgmt`, run:

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

## Architecture

- `src/test/java/com/example/management/api/` — API-focused examples and future endpoint tests
- `src/test/java/com/example/management/support/fixtures/` — shared Spring Boot test bases
- `src/test/java/com/example/management/support/helpers/` — auth and HTTP helpers
- `src/test/java/com/example/management/support/factories/` — request builders with override-friendly defaults
- `src/test/resources/` — test profile configuration

## Best practices

- Seed state through API helpers or factories, not through the browser UI.
- Keep tests independent; use unique usernames from factories to avoid collisions.
- Prefer JSON-path assertions and explicit status checks over brittle string parsing.
- Reuse `BaseIntegrationTest` for new API and integration tests.

## CI notes

- Use JDK 17 on the runner.
- Run `mvn test` from `user-mgmt`.
- Point `BASE_URL` and `API_URL` at the deployed service when adding browser or external API checks later.

## References

- TEA knowledge: data factories, fixture architecture, API testing patterns
- Project context: `user-mgmt/project-context.md`
