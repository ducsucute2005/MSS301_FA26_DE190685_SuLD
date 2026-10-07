# FU Cinema Booking System

A cinema ticket booking system built with Spring Boot services, SQL Server, MongoDB, MySQL, and Spring Cloud Gateway.

## Services and ports

| Component | Port | Database |
|---|---:|---|
| API Gateway | 9000 | — |
| Customer Service | 8081 | SQL Server (`cinema_customer`) |
| Movie Service | 8082 | MongoDB (`cinema_movie`) |
| Booking Service | 8083 | MySQL (`cinema_booking`) |

Postman requests should use the API Gateway at `http://localhost:9000`, rather than calling protected service endpoints directly.

## Prerequisites

- Java 21
- Maven
- Docker Desktop with Docker Compose
- Postman

## Start the databases

From this directory, start SQL Server, MongoDB, and MySQL:

```powershell
docker compose up -d
docker compose ps -a
```

Wait until `cinema-sqlserver` is healthy and `cinema-sqlserver-init` has completed successfully before starting the services.

## Start the application services

Start each service in a separate terminal, or run its `*Application` class from the IDE. Start them in this order:

1. Customer Service
2. Movie Service
3. Booking Service
4. API Gateway

To start a service from its directory:

```powershell
mvn spring-boot:run
```

The Movie Service seeds sample genres, rooms, movies, and showtimes in MongoDB when required. Booking Service calls Movie Service directly for showtime details.

Check that the gateway is available:

```text
GET http://localhost:9000/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## Test accounts

The Customer Service seed migration creates these local test accounts:

| Account | Role | Status | Notes |
|---|---|---|---|
| `admin@fucinema.com` | ADMIN | Active | Credentials are configured locally for Customer Service. |
| `an@gmail.com` | CUSTOMER | ACTIVE | Seeded customer, ID `1`. |
| `binh@gmail.com` | CUSTOMER | ACTIVE | Seeded customer, ID `2`. |
| `chi@gmail.com` | CUSTOMER | INACTIVE | Login should be rejected because the account is inactive. |

Use the test credentials specified by the assignment's local setup instructions. Do not add passwords, JWT secrets, or bearer tokens to this README, exported Postman files, screenshots, or Git commits. After login, Postman scripts should save access tokens in the local `FUCinema-Local` environment.

## Postman tests

1. Import `postman/FUCinemaBookingSystem.postman_collection.json` and `postman/FUCinema-Local.postman_environment.json` if exported files are available.
2. Select the `FUCinema-Local` environment and confirm `gateway` is `http://localhost:9000`.
3. Run the collection in folder order from `01-Auth` through `08-Report`. Requests create data and environment variables used by later requests, so do not run them out of order or in parallel.
4. The manual Movie Service outage scenario (request 6.15 in the assignment guide) is not part of the normal Collection Runner sequence.
5. Review the Runner results and capture a screenshot showing the pass/fail totals for the assignment report.

Keep saved access tokens out of exported environment files before committing them.

## Troubleshooting

- If a service cannot connect to its database, make sure Docker Compose is running and the relevant container is ready.
- If the gateway returns `401`, obtain a fresh token through the login endpoint and check that the JWT secret is configured consistently in Customer Service and API Gateway.
- If a protected endpoint reports a missing `X-User-Id` header, send the request through the API Gateway.
- If Lombok-generated methods appear unresolved in the IDE, enable Lombok annotation processing and reload the Maven projects.
- For Flyway, MongoDB, or Postman scenario details, refer to `../../Assignment1_Guide.md`.
