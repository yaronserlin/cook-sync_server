---
name: cooksync-project-guidelines
applyTo: "**/*.java"
description: "Use project-specific backend conventions for the CookSync recipe-sharing application. Prefer Spring Boot REST controllers, service-layer business logic, JPA entities, JWT-based auth, MySQL-compatible model patterns, and the existing recipe/favorites/review domain."
---

# CookSync Project Instructions

These instructions guide the AI assistant when working on the CookSync backend.

## Project backend summary
- The backend is a Spring Boot REST API for a recipe-sharing and management system.
- Use JWT authentication for user login and protected routes, with `isAdmin` controlling admin privileges.
- Persist data through JPA and MySQL-compatible entities, with JDBC-style database interaction via Spring Data JPA.
- Keep the backend aligned with the existing domain: users, recipes, ingredients, instructions, tags, reviews, favorites, personal notes, and recipe images.

## When editing or adding Java code
- Use `@RestController` for HTTP endpoints, `@Service` for business logic, and `@Repository` for data access.
- Keep controller methods thin: validate requests, authorize users, and delegate business rules to service classes.
- Preserve JWT-based authentication patterns using `JwtAuthenticationFilter`, `SecurityConfig`, and `JwtUtil`.
- Prefer `ResponseEntity` for controller responses and return correct HTTP status codes for create, update, delete, and error conditions.

## Database and entity conventions
- Use JPA entities with MySQL-compatible mappings and `java.time.LocalDateTime` for audit timestamps.
- Model relationships consistently with existing entities such as `User`, `Recipe`, `Tag`, `Ingredient`, `Instruction`, `Review`, `FavoriteRecipe`, `PersonalInstructionNote`, and `RecipeImage`.
- Represent recipe search and filtering criteria through repository queries or service-layer projections, not by embedding heavy SQL in controllers.
- Track audit fields (`createdAt`, `updatedAt`) on entities and maintain them in service logic or entity constructors.

## API and behavior expectations
- Implement user-facing behavior from the spec: sign-up/login, recipe search/filter, recipe creation/update/delete, favorites, reviews, personal notes, and admin moderation.
- Ensure admin-only operations are protected and regular users cannot delete or manage content they do not own or moderate.
- Support recipe filtering by tags, difficulty, prep/cook time, and rating, and preserve tag management for the admin role.
- Keep personal instruction notes private to the owning user and connect notes to recipes and optional instruction steps.

## Coding style and safety
- Avoid introducing new external frameworks beyond Spring Boot, JPA, JWT, Lombok, and the existing dependencies unless required for a clear feature need.
- Keep code simple, readable, and consistent with the current package structure under `com.cooksync_server`.
- Follow the existing security and exception-handling conventions in `GlobalExceptionHandler`, `SecurityConfig`, and controller/service patterns.
- Use environment-based configuration for database and JWT secrets, matching the current `application.properties` and `.env` usage.
