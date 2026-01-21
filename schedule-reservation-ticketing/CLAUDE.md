# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.4 microservice for concert/performance ticketing and reservation management (schedule-reservation-ticketing). Built with Java 21, it handles stadium/venue management, seat reservations, ticket issuance, and user authentication via OAuth2 (Kakao).

## Build and Development Commands

```bash
# Build the project
./gradlew build

# Run the application (port 8081)
./gradlew bootRun

# Run all tests (uses Testcontainers with PostgreSQL 15)
./gradlew test

# Run a single test class
./gradlew test --tests "wisoft.nextframe.schedulereservationticketing.service.ReservationServiceTest"

# Run a single test method
./gradlew test --tests "wisoft.nextframe.schedulereservationticketing.service.ReservationServiceTest.testMethodName"

# Clean build
./gradlew clean build

# Create executable JAR
./gradlew bootJar
```

## Architecture

### Layered Structure
```
Controller → Service/Facade → Repository → Entity
                ↓
           AOP (LoggingAspect)
                ↓
         PostgreSQL + Redis
```

### Source Organization
- `controller/` - REST endpoints (auth, performance, reservation, review, seat, stadium, ticketing)
- `service/` - Business logic with Facade pattern for complex operations (OAuthFacade, SeatStateFacade)
- `repository/` - JPA repositories with QueryDSL support
- `entity/` - JPA entities organized by domain (user, stadium, performance, reservation, schedule, seat, review, ticketing)
- `dto/` - Request/Response objects per domain
- `config/` - Spring configuration (jwt/, security/, db/)
- `common/` - Shared utilities (exception/, aop/, lock/, mapper/, response/, Money.java)

### Key Patterns
- **Value Object**: `Money` class for monetary amounts with validation
- **Facade Pattern**: `OAuthFacade`, `SeatStateFacade` simplify complex multi-service operations
- **Factory Pattern**: `ReservationFactory` for reservation creation
- **Distributed Locking**: `DistributedLockManager` using Redisson for concurrent seat reservations
- **Custom Error Handling**: `ErrorCode` enum + `DomainException` + `GlobalExceptionHandler`

### Database Configuration
- Primary datasource uses HikariCP with custom configuration (see `application-dev-db.yml`)
- Tests use Testcontainers with `jdbc:tc:postgresql:15:///test-db`
- DDL mode: `validate` in dev, `create-drop` in tests

### Caching
- Redis with Redisson for distributed caching and locking
- Key prefix: `next-frame:`

## Testing

Tests use JUnit 5 with Testcontainers for PostgreSQL. The test profile (`application-test.yml`) auto-creates the schema.

```bash
# All tests
./gradlew test

# Specific test patterns
./gradlew test --tests "*Controller*"
./gradlew test --tests "*Service*"
./gradlew test --tests "*Repository*"
```

## API Documentation

Swagger UI available at `/swagger-ui.html` when running (SpringDoc OpenAPI 2.8.13).

## Key Dependencies
- QueryDSL 5.0.0 (Jakarta) for type-safe queries
- jjwt 0.11.5 for JWT authentication
- Redisson 3.27.2 for distributed locks/cache
- Google Zxing 3.5.3 for QR code generation
- Testcontainers for integration tests
