---
name: run-cook-sync-server
description: Build, launch, and drive the CookSync Spring Boot backend (REST API on port 8080, MySQL-backed). Use when asked to run/start/build the server, or to verify a server-side change actually works (register/login/recipes endpoints) rather than just passing unit tests. Drives the running API with curl via smoke.sh.
---

Paths below are relative to `cook-sync-server/` (this skill's unit).

CookSync's backend is a Spring Boot REST API (Java 17+, Maven, Spring Data JPA,
MySQL). It has no UI of its own — it's driven with HTTP requests. The primary
agent path is [smoke.sh](smoke.sh), a curl-based driver that boots against a
running instance and exercises register → login → authenticated call → public
recipe listing.

## Prerequisites

- MySQL running on `localhost:3306` with a `cooksync_db` schema, and credentials
  matching `.env` at the repo root (`DB_USERNAME` / `DB_PASSWORD`). On this
  machine MySQL runs as a persistent LaunchDaemon (Oracle's `.pkg` installer,
  not Homebrew) — it's already running and already has the schema populated;
  you shouldn't need to do anything. Verify with:
  ```
  /usr/local/mysql/bin/mysql -u root -p"$(grep DB_PASSWORD .env | cut -d= -f2)" -e "USE cooksync_db; SHOW TABLES;"
  ```
  (Full path, not just `mysql` — this is the Oracle installer's client, and
  there may be nothing named `mysql` on `PATH` at all.) If that fails, MySQL
  isn't running / the schema doesn't exist yet — a real environment gap, not
  something this skill sets up for you.
- `.env` at the repo root must exist with `DB_USERNAME`, `DB_PASSWORD`,
  `JWT_SECRET` (and `CLOUDINARY_*`, only needed for image-upload endpoints).
  `application.properties` reads it via `spring.config.import=optional:file:.env`.
- Java 17+, Maven wrapper (`./mvnw`, no separate Maven install needed).

## Build

```
./mvnw -q -DskipTests package
```
Produces `target/cook-sync-0.0.1-SNAPSHOT.jar`. Verified: builds clean in this
repo as-is.

## Run (agent path)

Check first whether something is already listening on 8080 — **a long-running
dev instance is commonly already up on this machine** (started outside this
skill). If so, just point `smoke.sh` at it; don't kill someone's existing
session to launch your own.

```
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/recipes/public
```
`200` means a server is already up. Otherwise launch one:

```
cd cook-sync-server
nohup ./mvnw -q -DskipTests spring-boot:run > /tmp/cook-sync-server.log 2>&1 &
```
Boots in ~10s. There's no `/actuator/health` (no actuator dependency) — poll
`GET /api/recipes/public` for `200` as the readiness check (`smoke.sh` already
does this, 2s interval, 30 tries).

Then drive it:
```
.claude/skills/run-cook-sync-server/smoke.sh                    # against localhost:8080
.claude/skills/run-cook-sync-server/smoke.sh http://localhost:8099  # or any other base URL
```
It registers a throwaway user (`smoke+<timestamp>@example.com`), logs in,
calls the authenticated `validate-token` endpoint with the JWT, then checks
both public recipe list endpoints. Prints `PASS:`/`FAIL:` per step and exits
non-zero on first failure.

To launch your **own** isolated instance instead of reusing one that's already
running (e.g. to test a source change under a clean process), override the
port so you don't collide with an existing dev instance on 8080:
```
nohup ./mvnw -q -DskipTests spring-boot:run -Dspring-boot.run.arguments=--server.port=8099 \
  > /tmp/cook-sync-server-test.log 2>&1 &
# wait for readiness, then:
.claude/skills/run-cook-sync-server/smoke.sh http://localhost:8099
# when done:
pkill -f 'spring-boot.run.arguments=--server.port=8099'
```

## Run (human path)

`./run_project.sh` from the repo root — also rewrites the Android client's
`BASE_URL`/network-security-config to your LAN IP and starts the server via
`./mvnw spring-boot:run` in the foreground. Only useful if you're also about
to run the Android client against this same machine; for API-only work the
agent path above is simpler and doesn't touch client files.

## Test

```
./mvnw -q test
```

## Gotchas

- **A long-running dev instance is often already on 8080, and it can be stale.**
  Verified live: a pre-existing instance was running from an older build and
  returned 404 on `/api/recipes/public/paged` ("Recipe not found: paged" — the
  `/public/{id}` path-variable route was shadowing it) even though that
  endpoint exists in current source. If `smoke.sh` fails on the paged check
  but passes everything else, the running instance predates a recent
  controller change — rebuild and relaunch (see the port-8099 pattern above)
  rather than assuming the endpoint is broken.
- **No `/actuator/health`** — spring-boot-actuator isn't a dependency. Don't
  wait on it; poll a real endpoint like `/api/recipes/public`.
- **Two separate local MySQL installs can coexist and fight over port 3306.**
  This machine has a real, already-configured MySQL (Oracle installer,
  LaunchDaemon, `RunAtLoad`/`KeepAlive`, already has `cooksync_db`). Installing
  MySQL via Homebrew as well and running `brew services start mysql` starts a
  second `mysqld` that fails to bind port 3306 (already in use) and a second
  one on 33060 (mysqlx) — harmless failure, but don't be misled into thinking
  MySQL isn't running because of it. There should be no need to `brew install
  mysql` on this machine at all.
- Password-validation on `/api/auth/register` is strict:
  `^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$` — a
  plain lowercase password will 400. `smoke.sh` uses `Passw0rd!`.
