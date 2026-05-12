---
stepsCompleted:
  - step-01-preflight
  - step-02-select-framework
  - step-03-scaffold-framework
  - step-04-docs-and-scripts
  - step-05-validate-and-summary
lastStep: step-05-validate-and-summary
lastSaved: 2026-05-12
inputDocuments:
  - _bmad-output/test-artifacts/test-design/test-design-epic-user-mgmt-performance.md
---

# Framework Setup Progress

## Step 1: Preflight

- Detected stack: `backend`
- Target application: `user-mgmt` (Spring Boot 3.5.6, Java 17, Maven)
- Existing tests: `ApplicationTests`, `AuthenticationTest`, `ExampleAuthApiTest`
- Performance design: `_bmad-output/test-artifacts/test-design/test-design-epic-user-mgmt-performance.md`
- Architecture context: `user-mgmt/project-context.md`, `user-mgmt/PROJECT_DOCUMENTATION.md`
- No Playwright or Cypress config present (browser load out of scope per design)

## Step 2: Framework Selection

- Functional framework: **JUnit 5** with Spring Boot Test and MockMvc
- Performance framework: **k6** for API/static load, stress, and smoke NFR validation
- Rationale: Java backend with existing Spring Security and REST auth endpoints; concurrent load delegated to k6 per TEA NFR guidance

## Step 3: Scaffold

- Functional support packages under `src/test/java/com/example/management/support/`
- Sample API test: `api/ExampleAuthApiTest.java`
- `application-test.properties`, `.env.example`, and `.java-version`
- k6 layout under `tests/nfr/performance/` with shared `lib/` (config, auth, thresholds, summary)
- P0 scripts: `pf01-admin-list.k6.js` through `pf04-mixed-admin.k6.js`; P1 smoke: `smoke.k6.js`
- Gherkin traceability: `tests/nfr/performance/gherkin/user-mgmt-performance.feature`
- Spring `perf` profile: `src/main/resources/application-perf.properties`
- Summary archival target: `_bmad-output/test-artifacts/performance/`

## Step 4: Documentation and Scripts

- Updated `user-mgmt/tests/README.md` with functional and k6 architecture
- Primary functional command: `.\mvnw.cmd test`
- Performance runners: `tests/nfr/performance/scripts/run-smoke.ps1`, `run-p0.ps1`

## Step 5: Validation

- Checklist reviewed against backend + k6 scaffold aligned to performance epic design
- Functional gate: `mvn test` from `user-mgmt`
- Performance gate: k6 smoke/P0 against app with `perf` profile; summaries written to artifact dir
