# Java Workout Planner

A full-stack workout planning application built with Spring Boot and React.

## Project Structure

```
java-workout-planner/
├── backend/          # Spring Boot REST API
├── frontend/         # React web application
└── README.md         # This file
```

## Tech Stack

### Backend
- Java 17+
- Spring Boot 3.x
- Spring Security with Auth0
- Spring Data JPA
- PostgreSQL
- Liquibase (Database migrations)
- Maven

### Frontend
- React 18
- JavaScript/JSX
- Create React App

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 18+ and npm
- PostgreSQL (or H2 for development)
- Maven (or use included Maven wrapper)

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Configure your database in `src/main/resources/application.properties`

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

   The API will be available at `http://localhost:8080`

4. View API documentation:
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - OpenAPI spec: `http://localhost:8080/v3/api-docs`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

   The app will open at `http://localhost:3000`

## Development

### Running Tests

**Backend:**
```bash
cd backend
./mvnw test
```

**Frontend:**
```bash
cd frontend
npm test
```

### Building for Production

**Backend:**
```bash
cd backend
./mvnw clean package
```

**Frontend:**
```bash
cd frontend
npm run build
```

## API Documentation

The backend API is documented using OpenAPI 3.0 (Swagger). When the backend is running, visit:
- Interactive docs: `http://localhost:8080/swagger-ui.html`

## Features

- User authentication and authorization (Auth0)
- Workout session management
- Exercise tracking (strength, cardio, flexibility)
- Set-based exercise logging
- User profile management
- Audit logging

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests
4. Submit a pull request

## License

[Your License Here]