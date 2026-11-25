# Citizen Registry RESTful Service

Multi-module Spring Boot (Maven) project implementing a Citizen Registry RESTful API, console client, automated unit/API tests, and a GitHub Actions CI/CD pipeline.

## Project Structure

- `citizen-domain` — Domain entity (`Citizen`) with Jakarta Persistence & Validation constraints.
- `citizen-service` — Spring Boot RESTful service, H2 persistence, exception handling, unit tests (`*Test`), and API/integration tests (`*IT`).
- `citizen-client` — Interactive CLI client using Spring `RestClient`.
- `.github/workflows/ci-cd.yml` — GitHub Actions CI/CD pipeline triggered on pushes to `develop`.

## REST API Endpoints (`/api/citizens`)

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/citizens` | Create a new citizen (`201 Created`, `400`, `409`) |
| `GET` | `/api/citizens` | List all citizens or filter using query parameters (`id`, `firstName`, `lastName`, `gender`, `birthDate`, `taxId`, `address`) |
| `GET` | `/api/citizens/{id}` | Retrieve a citizen by 8-character ID (`200 OK`, `400`, `404`) |
| `PATCH` | `/api/citizens/{id}` | Update `taxId` and/or `address` (`200 OK`, `400`, `404`) |
| `DELETE` | `/api/citizens/{id}` | Delete a citizen by ID (`204 No Content`, `400`, `404`) |

## Build & Test Commands

```bash
# Compile all modules
mvn clean test-compile

# Run unit, repository, context, and configuration tests (Surefire)
mvn test

# Package executable JARs and run API/integration tests (Failsafe)
mvn verify
```

Test reports are generated in:
- Unit tests: `citizen-service/target/surefire-reports/`
- API / Integration tests: `citizen-service/target/failsafe-reports/`

## Running Locally

```bash
# Start the REST service
java -jar citizen-service/target/citizen-service-1.0.0.jar

# Start the CLI client (in a second terminal)
java -jar citizen-client/target/citizen-client-1.0.0.jar
```

## CI/CD Pipeline

The workflow at `.github/workflows/ci-cd.yml` runs automatically on commits pushed to the `develop` branch:
1. Checks out the repository and sets up JDK 17 with Maven caching.
2. Compiles source and test code (`mvn -B clean test-compile`).
3. Runs unit tests (`mvn -B test`) and uploads Surefire reports as the `unit-test-reports` artifact.
4. Packages executable JARs, runs API/integration tests (`mvn -B verify -DskipUnitTests=true`), and uploads Failsafe reports as the `integration-test-reports` artifact.
5. Publishes the built executable JARs to GitHub Releases as a pre-release (`v1.0.0-dev.<run_number>`).
