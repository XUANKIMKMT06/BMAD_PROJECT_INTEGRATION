@user-mgmt @performance @nfr
Feature: User Management performance NFR
  Performance test cases for the user-mgmt website.
  Executable automation: k6 scripts under tests/nfr/performance/.
  Design reference: _bmad-output/test-artifacts/test-design/test-design-epic-user-mgmt-performance.md

  Background:
    Given the user-mgmt service is reachable at BASE_URL
    And the Spring Boot perf profile is active
    And k6 is installed on the test runner

  @TC-PF-05 @TC-PF-09 @P1 @smoke @script:smoke.k6.js
  Scenario: API and static asset smoke under light load
  # PF-05 / PF-09 — smoke.k6.js — 5 VUs, 60s
    Given an admin account is available with ADMIN_USER and ADMIN_PASSWORD
    And an admin JWT has been obtained in setup
    When 5 virtual users repeatedly for 60 seconds:
      | step | method | path                         |
      | 1    | GET    | /                            |
      | 2    | GET    | /css/style.css               |
      | 3    | POST   | /api/v1/auth/authenticate    |
      | 4    | GET    | /dashboard/users             |
    And each authenticated request sends Authorization Bearer <admin JWT>
  # Authenticate step uses ADMIN_USER and ADMIN_PASSWORD in the request body.
    Then the response status for GET / is 200
    And the response status for GET /css/style.css is 200
    And the response status for POST /api/v1/auth/authenticate is 200
    And the response status for GET /dashboard/users is 200
    And the response body for GET /dashboard/users is a JSON array
    And the HTTP failure rate is less than 1%
    And the p95 latency is less than 800ms for GET /
    And the p95 latency is less than 800ms for GET /css/style.css
    And the p95 latency is less than 800ms for POST /api/v1/auth/authenticate
    And the p95 latency is less than 800ms for GET /dashboard/users

  @TC-PF-01 @P0 @script:pf01-admin-list.k6.js
  Scenario: Admin user list under sustained load
  # PF-01 — pf01-admin-list.k6.js — 30 VUs, 5m
    Given an admin JWT has been obtained in setup
    When 30 virtual users repeatedly send GET /dashboard/users for 5 minutes
    And each request sends Authorization Bearer <admin JWT>
    Then the response status is 200
    And the response body is a JSON array
    And the HTTP failure rate is less than 1%
    And the p95 latency for GET /dashboard/users is less than 300ms

  @TC-PF-02 @P0 @script:pf02-login-storm.k6.js
  Scenario: Login storm under ramping concurrency
  # PF-02 — pf02-login-storm.k6.js — ramp 10 to 50 VUs over 5m30s
    Given valid credentials exist for ADMIN_USER and ADMIN_PASSWORD
    When virtual users ramp from 10 to 50 over 1 minute
    And remain at 50 virtual users for 4 minutes
    And ramp down to 0 over 30 seconds
    And each virtual user repeatedly sends POST /api/v1/auth/authenticate with those credentials
    Then the response status is 200
    And the response includes a JWT token
    And the HTTP failure rate is less than 1%
    And the p95 latency for POST /api/v1/auth/authenticate is less than 500ms

  @TC-PF-03 @P0 @script:pf03-home.k6.js
  Scenario: Authenticated home page under sustained load
  # PF-03 — pf03-home.k6.js — 50 VUs, 5m
    Given a valid JWT has been obtained in setup
    When 50 virtual users repeatedly send GET /home for 5 minutes
    And each request sends Authorization Bearer <JWT>
    Then the response status is 200
    And the response body contains HTML
    And the HTTP failure rate is less than 1%
    And the p95 latency for GET /home is less than 400ms

  @TC-PF-04 @P0 @script:pf04-mixed-admin.k6.js
  Scenario: Mixed admin read and write workload
  # PF-04 — pf04-mixed-admin.k6.js — 30 VUs, 5m
    Given an admin JWT has been obtained in setup
    When 30 virtual users run a mixed workload for 5 minutes
    And approximately 70% of iterations send GET /dashboard/users with the admin JWT
    And approximately 20% of iterations send POST /api/v1/auth/register with a unique username
    And approximately 10% of iterations register a disposable user then send DELETE /delete/{username} with the admin JWT
    Then successful happy-path responses return HTTP 200
    And the HTTP failure rate is less than 1%
    And the global p95 latency is less than 500ms

  @TC-PF-06 @P1 @planned
  Scenario: Register burst spike
  # Planned — not yet implemented as k6 script
    Given the user-mgmt service is reachable at BASE_URL
    When virtual users spike from 0 to 40 in 30 seconds
    And remain at 40 virtual users for 2 minutes
    And each registration uses a unique username
    Then no HTTP 5xx responses occur
    And the p99 latency for POST /api/v1/auth/register is less than 1500ms

  @TC-PF-07 @P1 @planned
  Scenario: Invalid JWT volume does not destabilize the service
  # Planned — not yet implemented as k6 script
    Given the user-mgmt service is reachable at BASE_URL
    When 20 virtual users repeatedly send GET /dashboard/users with missing or invalid JWTs
    Then responses are 401 or 403
    And no HTTP 5xx responses occur

  @TC-PF-08 @P1 @planned
  Scenario: Logout churn under load
  # Planned — not yet implemented as k6 script
    Given valid user credentials exist
    When 20 virtual users repeatedly authenticate and call GET /api/v1/auth/logout
    Then logout happy-path responses return HTTP 200
    And the p95 latency for GET /api/v1/auth/logout is less than 400ms
