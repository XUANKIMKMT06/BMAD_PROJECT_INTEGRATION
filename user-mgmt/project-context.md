# Project Context - User Management

## Project Summary

`user-management` is a Spring Boot web app for user lifecycle and access control. It currently supports registration, login/logout, JWT-based stateless authentication, and role-based authorization (`ROLE_USER`, `ROLE_ADMIN`) with an admin dashboard.

## Current Scope

- User auth: register, authenticate, logout
- Authorization: protected routes and admin-only routes
- Admin actions: list users, delete users, promote users to admin
- UI delivery: static HTML/JavaScript pages served by backend

## Architecture Snapshot

- Backend: Java 21, Spring Boot 3.5.6, Spring Security, Spring JDBC
- Frontend: Vanilla JavaScript + Bootstrap
- Auth model: JWT issued on auth, validated by filter, invalidated on logout
- Data: in-memory H2 with SQL schema/seed (`schema.sql`)

## Key Endpoints

- Auth API:
  - `POST /api/v1/auth/authenticate`
  - `POST /api/v1/auth/register`
  - `GET /api/v1/auth/logout`
- Admin API:
  - `GET /dashboard/users`
  - `DELETE /delete/{user}`
  - `POST /make-admin/{email}`

## Important Constraints and Risks

- JWT secret is currently in app properties (not production-safe).
- Token is stored in browser `localStorage` (higher XSS exposure).
- H2 in-memory DB is non-persistent (development-oriented only).
- Debug logging is enabled broadly and should be reduced for production.

## Suggested Product/Technical Direction

1. Move to persistent DB (PostgreSQL/MySQL) with migrations.
2. Externalize secrets and environment-specific configuration.
3. Add integration tests for auth and admin-protected flows.
4. Strengthen token lifecycle (refresh token + tighter revocation).
5. Add API documentation and deployment guide.

## Source of Truth

This context is derived from `PROJECT_DOCUMENTATION.md` and should be updated when that document changes.
