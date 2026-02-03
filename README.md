# Workout Planner

Full-stack fitness tracking application for managing workouts, exercises, and training sets.

## Tech Stack

### Backend

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, Auth0 OAuth2, JWT (RS256) |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Liquibase |
| Build | Maven 3.11 |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Rate Limiting | Bucket4j |
| Caching | Caffeine |
| Monitoring | Micrometer, Prometheus |
| Code Gen | MapStruct, Lombok |
| Testing | JUnit 5, Mockito, Testcontainers, REST Assured, H2 |
| Code Quality | SonarQube, JaCoCo |

### Frontend

| Category | Technology |
|----------|------------|
| Language | TypeScript 5.6 |
| Framework | React 18 |
| Build | Vite 5.4 |
| Styling | Tailwind CSS 4.1 |
| Auth | Auth0 React SDK |
| HTTP Client | Axios |
| Routing | React Router v6 |
| Testing | Vitest, React Testing Library |
| Linting | ESLint 9 |

### Infrastructure

| Category | Technology |
|----------|------------|
| Database | PostgreSQL 15 (Docker) |
| Code Quality | SonarQube (Docker) |
| Containers | Docker Compose |

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 15+ (or use Docker)

### Database

```bash
cd backend
docker-compose up -d  # Starts PostgreSQL on port 5432
```

### Backend

```bash
cd backend

# Set required environment variables
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export AUTH0_ISSUER_URI=https://your-tenant.auth0.com/
export AUTH0_AUDIENCE=your-api-audience

./mvnw spring-boot:run
```


### Frontend

```bash
cd frontend
npm install
npm run dev
```

App runs on `http://localhost:3000`

## API Documentation

Swagger UI available at `http://localhost:8081/swagger-ui.html` (dev mode)

## Testing

```bash
# Backend (includes Testcontainers integration tests)
cd backend && ./mvnw test

# Frontend
cd frontend && npm test
```

## Project Structure

```
├── backend/           # Spring Boot REST API
│   ├── controller/    # REST endpoints
│   ├── service/       # Business logic
│   ├── entity/        # JPA entities
│   ├── repository/    # Data access
│   └── security/      # Auth0 integration
│
└── frontend/          # React SPA
    ├── pages/         # Route components
    ├── components/    # Reusable UI
    ├── services/      # API client
    └── auth/          # Auth0 provider
```

## Features

- Workout session management (create, track, complete)
- Exercise library with three types: strength, cardio, flexibility
- Set-based logging with type-specific metrics
- User authentication via Auth0
- Role-based access control
- Rate limiting and security headers


