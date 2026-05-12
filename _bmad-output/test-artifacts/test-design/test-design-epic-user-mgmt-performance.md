---
workflowStatus: completed
workflowType: testarch-test-design
mode: epic-level
epic_num: user-mgmt-performance
lastSaved: '2026-05-12'
inputDocuments:
  - user-mgmt/PROJECT_DOCUMENTATION.md
  - user-mgmt/project-context.md
  - user-mgmt/tests/README.md
  - user-mgmt/src/main/resources/application.properties
  - user-mgmt/src/main/resources/application-perf.properties
  - user-mgmt/tests/nfr/performance/
---

# Test Design: Epic user-mgmt-performance — User Management Performance NFR

**Date:** 2026-05-12  
**Author:** Kimtx  
**Status:** Draft

---

## Executive Summary

**Scope:** Epic-level test design for **performance NFR validation** of the `user-mgmt` Spring Boot website (static UI + JSON APIs). Functional correctness remains covered by existing JUnit/MockMvc suites; this plan defines load, stress, soak, and smoke performance coverage with k6.

**Risk summary**

- Total risks identified: 10
- High-priority risks (score ≥ 6): 4
- Critical categories: PERF, DATA, SEC

**Coverage summary (performance scenarios)**

| Priority | Scenarios | Effort (range) |
| --- | --- | --- |
| P0 | 4 | ~8–12 hours |
| P1 | 5 | ~10–16 hours |
| P2 | 4 | ~6–10 hours |
| P3 | 2 | ~2–4 hours |
| **Total** | **15** | **~26–42 hours (~4–6 days)** |

---

## Not in Scope

| Item | Reasoning | Mitigation |
| --- | --- | --- |
| Full functional regression (auth, RBAC, admin CRUD) | Already owned by JUnit/MockMvc under `src/test/java` | Run `mvn test` on every commit |
| Browser E2E load (multi-user UI) | Playwright is unsuitable for concurrent load; TEA NFR guidance mandates k6 for system performance | k6 hits HTTP APIs and static assets; optional single-user Lighthouse for CWV |
| Production database benchmarking | Current app uses in-memory H2 | Re-baseline on PostgreSQL/MySQL before production performance gate (R-003) |
| Third-party CDN / external IdP | Monolith has no external auth or CDN in repo | N/A |
| Chaos / fault injection | Out of epic scope | Defer to reliability workflow if added later |

---

## Risk Assessment

### High-priority risks (score ≥ 6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R-001 | PERF | `GET /dashboard/users` scans full `USER_TABLE` without pagination | 3 | 2 | 6 | PF-01 + PF-10 at 1k/10k rows; add pagination before scale | Dev | Before scale release |
| R-002 | PERF | Each authenticated request: JWT verify + `TOKEN_TABLE` lookup + user load | 3 | 2 | 6 | PF-02, PF-03 under concurrent VUs; profile filter chain | Dev | Pre-release |
| R-003 | DATA | H2 in-memory is not representative of production DB latency/concurrency | 2 | 3 | 6 | Repeat P0 on persistent DB in perf environment | Dev/Ops | Before prod gate |
| R-004 | SEC | Performance runs against DEBUG logging may hide real latency and leak sensitive logs | 2 | 3 | 6 | Mandate `perf` Spring profile for all k6 runs | QA | Immediate |

### Medium-priority risks (score 3–4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| --- | --- | --- | --- | --- | --- | --- | --- |
| R-005 | PERF | BCrypt on login/register limits throughput under burst | 2 | 2 | 4 | PF-02, PF-06; document CPU saturation | QA |
| R-006 | OPS | No Actuator/metrics endpoints for correlating k6 with JVM | 2 | 2 | 4 | Add Micrometer; scrape during soak | Dev |
| R-007 | PERF | `TOKEN_TABLE` growth during soak may slow auth | 2 | 2 | 4 | PF-12 soak + PF-08 logout churn | QA |
| R-008 | BUS | Large admin list payloads slow browser (client-side search) | 1 | 3 | 3 | Track response size in PF-01 | QA |

### Low-priority risks (score 1–2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| --- | --- | --- | --- | --- | --- | --- |
| R-009 | PERF | Static asset cache headers not validated | 1 | 2 | 2 | PF-09 / Lighthouse spot check |
| R-010 | OPS | k6 runner network distance skews latency | 1 | 2 | 2 | Run k6 co-located with app in CI |

### Risk category legend

- **TECH:** architecture, integration, scalability
- **SEC:** access control, secrets, data exposure
- **PERF:** SLA/SLO, degradation, resource limits
- **DATA:** integrity, persistence realism
- **BUS:** user-visible harm, logic impact
- **OPS:** deployment, config, monitoring

---

## Entry Criteria

- [ ] Stakeholders acknowledge proposed SLO baselines (see Quality Gate) or accept **CONCERNS** default
- [ ] App reachable at `BASE_URL` from k6 runner
- [ ] `perf` profile used (`application-perf.properties`; not default DEBUG logging)
- [ ] Seed admin credentials documented (`alice@mail.co` / `pass` or env overrides)
- [ ] k6 installed on runner (v0.47+; validated with v2.0.0-rc1 locally)
- [ ] Scripts present under `user-mgmt/tests/nfr/performance/`

## Exit Criteria

- [ ] P0 k6 scenarios pass thresholds or approved waivers exist
- [ ] k6 summary JSON archived under `_bmad-output/test-artifacts/performance/`
- [ ] High risks R-001–R-004 have owners and mitigation status recorded
- [ ] No open 5xx under nominal P0 load
- [ ] Soak (P2) shows no runaway JVM heap trend when executed

---

## Test Coverage Plan

### Proposed SLO baselines (pending PM sign-off)

| Surface | Metric | Target | Tool |
| --- | --- | --- | --- |
| Auth API | p95 latency | < 500 ms | k6 on `POST /api/v1/auth/authenticate` |
| Auth API | p99 latency | < 1000 ms | k6 |
| Admin list API | p95 latency | < 300 ms | k6 on `GET /dashboard/users` |
| Protected page | p95 latency | < 400 ms | k6 on `GET /home` |
| Global happy paths | Error rate | < 1% | k6 `http_req_failed` |
| Static smoke | p95 per asset | < 800 ms (smoke) | k6 PF-09 |
| UI perception | LCP | < 2.5 s | Lighthouse (P3, single user) |

### P0 (critical) — nightly / pre-release

**Criteria:** Core admin and auth paths + high PERF risk + no workaround for load validation

| Requirement | Test level | Risk link | Script | Load model | Notes |
| --- | --- | --- | --- | --- | --- |
| Admin lists all users | API load | R-001 | `pf01-admin-list.k6.js` | 30 VUs, 5 min | Admin JWT in `setup()` |
| Login under concurrency | API load | R-002, R-005 | `pf02-login-storm.k6.js` | Ramp 10→50 VUs, 5 min | Reuses seed admin |
| Authenticated home page | API load | R-002 | `pf03-home.k6.js` | 50 VUs, 5 min | Bearer on `GET /home` |
| Mixed admin read/write | API load | R-001, R-005 | `pf04-mixed-admin.k6.js` | 30 VUs, 5 min | 70% list / 20% register / 10% delete |

**Total P0:** 4 scenarios, ~8–12 hours (script tuning + env + analysis)

### P1 (high) — PR smoke / short runs

| Requirement | Test level | Risk link | Script | Load model | Notes |
| --- | --- | --- | --- | --- | --- |
| API + static smoke | API + static | R-004 | `smoke.k6.js` | 5 VUs, 60 s | Landing, CSS, auth, admin list |
| Register burst | API spike | R-005 | (planned) | 0→40 VUs in 30 s | Add dedicated script |
| Invalid JWT volume | API load | R-002 | (planned) | 20 VUs | Expect 401/403, no 5xx |
| Logout churn | API load | R-007 | (planned) | 20 VUs | Login → logout loop |
| Static landing assets | Static | R-009 | `smoke.k6.js` | Included in smoke | `GET /`, `/css/style.css` |

**Total P1:** 5 scenarios, ~10–16 hours

### P2 (medium) — weekly

| Requirement | Test level | Risk link | Notes |
| --- | --- | --- | --- |
| User table scale | API load | R-001 | Seed 1k/10k users; repeat PF-01 |
| Stress to breaking point | API stress | R-005 | Increase VUs until error rate > 5% or p95 > 2× baseline |
| Soak endurance | API soak | R-007 | 15–20 VUs, 2 h mixed read |
| Make-admin under load | API load | R-001 | `POST /make-admin/{email}` concurrent with list |

**Total P2:** 4 scenarios, ~6–10 hours

### P3 (low) — on demand

| Requirement | Test level | Notes |
| --- | --- | --- |
| Lighthouse CWV | UI benchmark | `/`, `/login`, `/dashboard` cold/warm |
| Release baseline | Benchmark | Store k6 `summary.json` per tag |

**Total P3:** 2 scenarios, ~2–4 hours

---

## Execution Order

### Smoke (< 3 min)

- [ ] `smoke.k6.js` — landing, CSS, authenticate, admin list

### P0 (< 25 min per full run)

- [ ] `pf01-admin-list.k6.js`
- [ ] `pf02-login-storm.k6.js`
- [ ] `pf03-home.k6.js`
- [ ] `pf04-mixed-admin.k6.js`

### P1 / P2 / P3

- [ ] Add remaining P1 scripts before enforcing PR gate
- [ ] Schedule P2 weekly; soak off critical path
- [ ] P3 Lighthouse on demand before major UI changes

**CI philosophy:** Run `mvn test` on every commit. Run k6 smoke on PR when runner and app are available (< 3 min). Defer full P0/P2 to nightly/weekly due to duration and environment cost.

---

## Resource Estimates

| Priority | Count | Hours/scenario (guide) | Total hours |
| --- | --- | --- | --- |
| P0 | 4 | 2.0–3.0 | ~8–12 |
| P1 | 5 | 2.0–3.2 | ~10–16 |
| P2 | 4 | 1.5–2.5 | ~6–10 |
| P3 | 2 | 1.0–2.0 | ~2–4 |
| **Total** | **15** | — | **~26–42 (~4–6 days)** |

### Prerequisites

**Test data**

- Seed users from `schema.sql`; synthetic users via `POST /api/v1/auth/register` with unique emails (`perf-{vu}-{iter}@mail.co`)
- Env: `BASE_URL`, `ADMIN_USER`, `ADMIN_PASSWORD`, `USER_PASSWORD`

**Tooling**

- k6 (load/stress/spike/soak)
- Maven + Spring Boot `perf` profile
- Optional: Lighthouse, JVM metrics (Micrometer)

**Environment**

- Dedicated perf host or local with `spring-boot.run.profiles=perf`
- Artifact dir: `_bmad-output/test-artifacts/performance/`

---

## Quality Gate Criteria

### Pass / fail thresholds

- **P0 pass rate:** 100% of k6 threshold checks on happy paths
- **P1 pass rate:** ≥ 95% or documented waiver
- **P2/P3:** informational unless promoted to gate
- **High-risk mitigations:** R-001–R-004 tracked before production performance sign-off

### Performance gate matrix

| Result | Condition |
| --- | --- |
| **PASS** | P0 SLOs met with k6 evidence; soak shows stable heap when run |
| **CONCERNS** | SLOs not ratified, H2-only evidence, or p95 within 90–100% of threshold |
| **FAIL** | P0 breach, sustained 5xx under nominal load, latency grows unbounded with user count |

### Coverage targets (performance epic)

- **Critical API paths:** 100% of P0 endpoints covered by k6
- **Auth filter path:** exercised under load (PF-02, PF-03)
- **Admin data path:** list + mixed mutations (PF-01, PF-04)
- **Edge load cases:** invalid JWT and soak deferred to P1/P2 until scripted

---

## Mitigation Plans

### R-001: Unbounded admin user list (score 6)

**Strategy:** Measure at scale; implement pagination or server-side limits before marketing/admin scale.  
**Owner:** Dev  
**Timeline:** Before user count exceeds agreed cap  
**Verification:** PF-01 + PF-10 p95 within SLO at 1k rows

### R-002: Per-request auth stack cost (score 6)

**Strategy:** Load-test JWT filter path; consider token lookup indexing/caching if p95 exceeds SLO.  
**Owner:** Dev  
**Timeline:** Pre-release  
**Verification:** PF-02 and PF-03 thresholds green

### R-003: H2 not production-representative (score 6)

**Strategy:** Re-run P0 against PostgreSQL/MySQL in perf environment.  
**Owner:** Dev/Ops  
**Timeline:** Before production performance gate  
**Verification:** Second baseline JSON compared to H2 run

### R-004: DEBUG logging invalidates perf runs (score 6)

**Strategy:** Enforce `perf` profile in docs and CI; block perf jobs using default profile.  
**Owner:** QA  
**Timeline:** Immediate  
**Verification:** Log level INFO/WARN in perf runs

---

## Assumptions and Dependencies

### Assumptions

1. Epic scope is **performance NFR** for `user-mgmt`; functional gates remain in unit/integration tests.
2. Numeric SLOs are proposals until PM approves; default gate is **CONCERNS**.
3. k6 drives HTTP load; browser concurrency is out of scope.

### Dependencies

1. Running Spring Boot instance with `perf` profile — required before any k6 job
2. k6 on CI agent — required for automated smoke/P0

### Risks to plan

- **Risk:** Long P0 duration blocks PR feedback  
  **Impact:** Slow merges  
  **Contingency:** PR runs `smoke.k6.js` only; full P0 nightly

---

## Interworking and Regression

| Component | Impact | Regression scope |
| --- | --- | --- |
| Spring Security / JWT | Auth latency under load | PF-02, PF-03, smoke auth |
| `UserRepository` | List/delete/register scale | PF-01, PF-04 |
| Static resources | Landing smoke | `smoke.k6.js` |
| JUnit/MockMvc suite | Functional baseline | `mvn test` must stay green |

---

## Appendix

### Knowledge base references

- `risk-governance.md`, `test-priorities-matrix.md`, `test-levels-framework.md`, `nfr-criteria.md`

### Related documents

- `user-mgmt/PROJECT_DOCUMENTATION.md`
- `user-mgmt/project-context.md`
- `user-mgmt/tests/README.md`

### Implementation inventory

| Script | Scenario ID |
| --- | --- |
| `tests/nfr/performance/smoke.k6.js` | PF-05 / PF-09 (partial) |
| `tests/nfr/performance/pf01-admin-list.k6.js` | PF-01 |
| `tests/nfr/performance/pf02-login-storm.k6.js` | PF-02 |
| `tests/nfr/performance/pf03-home.k6.js` | PF-03 |
| `tests/nfr/performance/pf04-mixed-admin.k6.js` | PF-04 |

---

**Generated by:** BMad TEA Agent — Test Architect Module  
**Workflow:** `bmad-testarch-test-design`  
**Version:** 4.0 (BMad v6)
