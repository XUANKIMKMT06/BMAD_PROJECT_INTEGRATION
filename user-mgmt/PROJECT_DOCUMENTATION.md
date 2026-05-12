# Project Documentation - User Management

## Overview

`user-management` is a Spring Boot web application that provides:

- User registration
- User login/logout
- JWT-based stateless authentication
- Role-based access (`ROLE_USER`, `ROLE_ADMIN`)
- Admin dashboard for user listing, deletion, and role promotion

The project serves static HTML/JS views and uses an in-memory H2 database initialized from SQL scripts.

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Spring Web
- Spring Security
- Spring JDBC
- H2 (in-memory)
- JWT (`io.jsonwebtoken`)
- Lombok
- Vanilla JavaScript + Bootstrap (frontend)
- Maven

## Project Structure

- `src/main/java/com/example/management/Application.java` - Spring Boot entrypoint
- `src/main/java/com/example/management/security/` - security config and filters
- `src/main/java/com/example/management/security/jwt/` - JWT lifecycle (issue/validate/invalidate)
- `src/main/java/com/example/management/auth/` - auth API and business logic
- `src/main/java/com/example/management/app/` - page routing + admin operations
- `src/main/java/com/example/management/user/` - user domain model and repository
- `src/main/resources/static/` - frontend HTML/CSS/JS assets
- `src/main/resources/schema.sql` - H2 schema and seed users
- `src/main/resources/application.properties` - app and datasource config

## Authentication and Authorization Flow

1. Client submits credentials via `/login` or `/api/v1/auth/register`.
2. Backend authenticates user and issues JWT.
3. JWT is stored on client (`localStorage`) and sent in `Authorization: Bearer <token>`.
4. `JwtAuthFilter` validates token on protected routes.
5. Admin-only routes require `ROLE_ADMIN`.
6. Logout marks token as invalidated in `TOKEN_TABLE`.

## HTTP Endpoints

### UI Routes

- `GET /` -> `index.html`
- `GET /login` -> login page
- `GET /signup` -> signup page
- `GET /home` -> authenticated home page
- `GET /dashboard` -> admin dashboard page
- `GET /403` -> forbidden page

### Auth API

- `POST /api/v1/auth/authenticate` - authenticate and return JWT + user DTO
- `POST /api/v1/auth/register` - create user and return JWT + user DTO
- `GET /api/v1/auth/logout` - invalidate current JWT

### Admin API

- `GET /dashboard/users` - list users (`ROLE_ADMIN`)
- `DELETE /delete/{user}` - delete user (`ROLE_ADMIN`)
- `POST /make-admin/{email}` - grant admin role to user

## Data Model

### `USER_TABLE`

- `id` (PK)
- `name`
- `username`
- `password` (bcrypt hash)
- `roles` (comma-separated role string)

### `TOKEN_TABLE`

- `id` (PK)
- `user_id`
- `token`
- `is_logged_out`

## Configuration

From `application.properties`:

- H2 datasource: `jdbc:h2:mem:userdb`
- SQL init enabled: `spring.sql.init.mode=always`
- JWT secret loaded from `jwt.secret`
- Debug logging enabled for JDBC/security/web/http

## Local Run

Prerequisites:

- JDK 21
- Maven 3.8+

Run:

```bash
mvn spring-boot:run
```

Build:

```bash
mvn -B package --file pom.xml
```

## Frontend Behavior

- `static/js/main.js`
  - Handles login/register/logout
  - Stores `user` and `jwt` in `localStorage`
  - Navigates to home/dashboard using fetched HTML
- `static/js/admin.js`
  - Loads users from `/dashboard/users`
  - Supports dashboard search, add user, delete user
  - Supports role promotion via `/make-admin/{email}`

## Notes and Risks

- JWT secret is currently declared directly in properties; move to environment variable or secrets manager for production.
- In-memory H2 is suitable for development only; production needs persistent database.
- `localStorage` token storage is easy but increases XSS impact; HttpOnly cookie strategy can reduce risk.
- Debug logging should be reduced in production environments.

## Suggested Next Improvements

1. Replace H2 with PostgreSQL/MySQL and add migration tooling.
2. Externalize secrets and environment-specific configuration profiles.
3. Add integration tests for auth and admin-protected routes.
4. Introduce refresh-token rotation and stricter logout/token revocation strategy.
5. Add API docs (OpenAPI/Swagger) and deployment instructions.
