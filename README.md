# 💪 Workout Planner API

A production-ready Spring Boot REST API for managing workout sessions, exercises, and fitness tracking. Built with enterprise-grade architecture following REST best practices, optimized for performance with N+1 query prevention, and secured with Spring Security.

## ✨ Key Highlights

- **RESTful API**: Industry-standard REST design with nested resources and proper HTTP semantics
- **Performance Optimized**: Entity Graph pattern for N+1 query prevention, batch loading, and smart fetching
- **Secure**: Spring Security integration with session-based authentication and role-based access control
- **Production Ready**: Comprehensive validation, error handling, optimistic locking, and audit trails
- **Clean Architecture**: Separation of concerns with DTOs, mappers, services, and repositories

## 🚀 Features

### Core Functionality
- ✅ **User Management**: Registration, authentication, profile management with security
- ✅ **Exercise Catalog**: Comprehensive database with 3 exercise types (Strength, Cardio, Flexibility)
- ✅ **Workout Sessions**: Full CRUD with status tracking (PLANNED, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED)
- ✅ **Set Tracking**: Polymorphic sets with type-specific data (reps/weight, duration/distance, duration/intensity)
- ✅ **Progress Tracking**: Session history, completion status, and performance metrics

### Technical Features
- ✅ **RESTful Design**: Nested resources (`/workouts/{id}/exercises`, `/workout-exercises/{id}/strength-sets`)
- ✅ **N+1 Prevention**: `@EntityGraph`, `@BatchSize`, and `@Fetch(SUBSELECT)` optimization
- ✅ **Smart Loading**: Conditional set loading based on exercise type
- ✅ **Pagination**: All list endpoints support pagination and sorting
- ✅ **Validation**: Comprehensive validation with custom validators and business rules
- ✅ **Error Handling**: Global exception handling with proper HTTP status codes
- ✅ **Audit Trail**: Automatic tracking of created/updated timestamps and users
- ✅ **Optimistic Locking**: Concurrent modification prevention with versioning

## 🏗️ Technology Stack

| Category | Technology |
|----------|-----------|
| **Backend** | Spring Boot 3.5.6 |
| **Java** | Java 17 |
| **Database** | PostgreSQL 15 |
| **ORM** | Spring Data JPA + Hibernate |
| **Security** | Spring Security (Session-based) |
| **Mapping** | MapStruct |
| **Validation** | Hibernate Validator |
| **Build Tool** | Maven |
| **Containerization** | Docker & Docker Compose |
| **Code Quality** | SonarQube |

## 📐 Architecture

### Entity Relationships

```
User (1) ──────────► (N) WorkoutSession
                           │
                           │ (1)
                           │
                           ▼
                        (N) WorkoutExercise ◄──────── (N) Exercise
                           │
                           │ (1)
                           │
                           ├──► (N) StrengthSet
                           ├──► (N) CardioSet
                           └──► (N) FlexibilitySet
```

### Project Structure

```
workoutplanner/
├── src/main/java/com/workoutplanner/workoutplanner/
│   ├── config/          # Security, Audit, Application configuration
│   ├── controller/      # REST controllers (8 controllers)
│   │   ├── AuthController
│   │   ├── UserController
│   │   ├── ExerciseController
│   │   ├── WorkoutSessionController
│   │   ├── BaseSetController (abstract)
│   │   ├── StrengthSetController
│   │   ├── CardioSetController
│   │   └── FlexibilitySetController
│   ├── dto/
│   │   ├── request/     # 14 Request DTOs
│   │   └── response/    # 8 Response DTOs
│   ├── entity/          # 9 JPA entities with audit support
│   ├── enums/           # 5 enums (UserRole, ExerciseType, etc.)
│   ├── exception/       # Custom exceptions and global handler
│   ├── mapper/          # MapStruct mappers
│   ├── repository/      # Spring Data JPA repositories with EntityGraph
│   ├── service/         # Business logic layer
│   ├── util/            # Utility classes
│   └── validation/      # Custom validators
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   ├── logback-spring.xml
│   └── db/changelog/    # Liquibase migrations
└── src/test/            # Test classes
```

## 📡 Complete API Reference

### Base URL
All endpoints are prefixed with: `/api/v1`

### Authentication Endpoints

```
POST   /api/v1/auth/login    - Authenticate user
POST   /api/v1/auth/logout   - Logout current user
```

### User Endpoints

```
POST   /api/v1/users                        - Create user (registration)
GET    /api/v1/users/{userId}               - Get user by ID (auth required)
GET    /api/v1/users/me                     - Get current user profile
GET    /api/v1/users?page=0&size=20         - List all users (paginated)
PUT    /api/v1/users/{userId}               - Update user profile
DELETE /api/v1/users/{userId}               - Delete user
GET    /api/v1/users/search?firstName=John  - Search users by first name
GET    /api/v1/users/check-username?username=john_doe  - Check username availability
GET    /api/v1/users/check-email?email=john@example.com - Check email availability
```

### Exercise Endpoints

```
POST   /api/v1/exercises                    - Create exercise
GET    /api/v1/exercises/{exerciseId}       - Get exercise by ID
GET    /api/v1/exercises?page=0&size=20     - List all exercises (paginated)
PUT    /api/v1/exercises/{exerciseId}       - Update exercise
DELETE /api/v1/exercises/{exerciseId}       - Delete exercise
GET    /api/v1/exercises/search?name=squat  - Search exercises by name
GET    /api/v1/exercises/filter?type=STRENGTH&difficultyLevel=INTERMEDIATE - Filter exercises
```

### Workout Session Endpoints

```
POST   /api/v1/workouts                     - Create workout session
GET    /api/v1/workouts/{sessionId}         - Get workout by ID
GET    /api/v1/workouts/{sessionId}/smart   - Get workout with optimized set loading
GET    /api/v1/workouts?page=0&size=20      - List all workouts (paginated)
GET    /api/v1/workouts/user/{userId}       - Get user's workouts
PUT    /api/v1/workouts/{sessionId}         - Update workout
DELETE /api/v1/workouts/{sessionId}         - Delete workout
PATCH  /api/v1/workouts/{sessionId}/status  - Update workout status (start/pause/complete)
POST   /api/v1/workouts/{sessionId}/exercises         - Add exercise to workout
GET    /api/v1/workouts/{sessionId}/exercises         - Get exercises in workout
```

### Set Endpoints (Nested Resources)

**Strength Sets:**
```
POST   /api/v1/workout-exercises/{workoutExerciseId}/strength-sets         - Create set
GET    /api/v1/workout-exercises/{workoutExerciseId}/strength-sets         - List all sets
GET    /api/v1/workout-exercises/{workoutExerciseId}/strength-sets/{setId} - Get specific set
PUT    /api/v1/workout-exercises/{workoutExerciseId}/strength-sets/{setId} - Update set
DELETE /api/v1/workout-exercises/{workoutExerciseId}/strength-sets/{setId} - Delete set
```

**Cardio Sets & Flexibility Sets:**
- Same pattern as Strength Sets, replace `/strength-sets` with `/cardio-sets` or `/flexibility-sets`

## 🔐 Authentication

The API uses **session-based authentication** with Spring Security:

### Login Example
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "your_password"
  }'
```

### Using Authenticated Endpoints
After login, the session cookie is automatically managed by the browser/client.

## 💡 API Usage Examples

### Complete Workout Flow

```bash
# 1. Register a user
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "athlete_mike",
    "email": "mike@example.com",
    "password": "your_secure_password",
    "firstName": "Mike",
    "lastName": "Johnson"
  }'
# Returns: { "userId": 1, ... }

# 2. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "athlete_mike",
    "password": "your_secure_password"
  }'

# 3. Create an exercise (if not exists)
curl -X POST http://localhost:8081/api/v1/exercises \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bench Press",
    "description": "Compound chest exercise",
    "type": "STRENGTH",
    "targetMuscleGroup": "CHEST",
    "difficultyLevel": "INTERMEDIATE"
  }'
# Returns: { "exerciseId": 5, ... }

# 4. Create a workout session
curl -X POST http://localhost:8081/api/v1/workouts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "name": "Monday Upper Body",
    "description": "Chest and triceps focus",
    "status": "PLANNED"
  }'
# Returns: { "sessionId": 42, ... }

# 5. Add exercise to workout
curl -X POST http://localhost:8081/api/v1/workouts/42/exercises \
  -H "Content-Type: application/json" \
  -d '{
    "exerciseId": 5,
    "orderInWorkout": 1,
    "notes": "Focus on controlled movement"
  }'
# Returns: { "workoutExerciseId": 123, ... }

# 6. Add sets to the exercise
curl -X POST http://localhost:8081/api/v1/workout-exercises/123/strength-sets \
  -H "Content-Type: application/json" \
  -d '{
    "setNumber": 1,
    "reps": 10,
    "weight": 135.0,
    "restTimeInSeconds": 90,
    "notes": "Warm-up set"
  }'

curl -X POST http://localhost:8081/api/v1/workout-exercises/123/strength-sets \
  -H "Content-Type: application/json" \
  -d '{
    "setNumber": 2,
    "reps": 8,
    "weight": 185.0,
    "restTimeInSeconds": 120
  }'

# 7. Start the workout
curl -X PATCH http://localhost:8081/api/v1/workouts/42/status \
  -H "Content-Type: application/json" \
  -d '{ "action": "start" }'

# 8. Complete the workout
curl -X PATCH http://localhost:8081/api/v1/workouts/42/status \
  -H "Content-Type: application/json" \
  -d '{ "action": "complete" }'

# 9. View workout with all sets (optimized query)
curl http://localhost:8081/api/v1/workouts/42/smart
```

## ⚡ Performance Optimizations

### N+1 Query Prevention

The API implements multiple strategies to prevent N+1 query problems:

```java
// Entity Graph for eager loading relationships
@EntityGraph(attributePaths = {"workoutExercise"})
List<StrengthSet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(Long workoutExerciseId);

// Batch loading for collections
@OneToMany(mappedBy = "workoutExercise")
@BatchSize(size = 20)
@Fetch(FetchMode.SUBSELECT)
private List<StrengthSet> strengthSets;

// Smart loading endpoint
GET /api/v1/workouts/{sessionId}/smart
// Loads only the set types that match each exercise type
```

### Query Optimization Results
- **Before**: 1 + N queries (for N sets, 101 queries for 100 sets)
- **After**: 1-3 queries total (90-99% reduction)

## 🛠️ Setup & Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (via Docker)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/asennesa/java-workout-planner.git
cd java-workout-planner
```

2. **Set up environment variables**
```bash
cd workoutplanner
cat > .env << EOF
# Database Configuration
POSTGRES_DB=sonar
POSTGRES_USER=sonar
POSTGRES_PASSWORD=your_secure_password_here
DB_USERNAME=sonar
DB_PASSWORD=your_secure_password_here

# SonarQube Configuration
SONAR_JDBC_URL=jdbc:postgresql://postgres:5432/sonar
SONAR_JDBC_USERNAME=sonar
SONAR_JDBC_PASSWORD=your_secure_password_here
EOF
```

> **Security Note**: Replace `your_secure_password_here` with a strong password. Never commit `.env` files to version control.

3. **Start services**
```bash
export $(cat .env | grep -v '^#' | xargs)
docker-compose up -d
```

4. **Run the application**
```bash
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8081`

### Docker Services
- **PostgreSQL**: Port 5432 (databases: `workout_planner`, `sonar`)
- **SonarQube**: Port 9000 (default credentials should be changed on first login)

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## 📈 Code Quality

Run SonarQube analysis:

```bash
# Start SonarQube (if not running)
docker-compose up -d

# Run analysis
./mvnw clean verify sonar:sonar

# View results
open http://localhost:9000
```

## 🔧 Development Commands

### Maven
```bash
./mvnw clean compile        # Compile
./mvnw spring-boot:run      # Run application
./mvnw test                 # Run tests
./mvnw clean package        # Build JAR
./mvnw jacoco:report        # Generate coverage
```

### Database
```bash
# Connect to database
docker exec -it workoutplanner-postgres psql -U sonar -d workout_planner

# View tables
\dt

# View specific table
\d+ users

# Exit
\q
```

### Docker
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild and start
docker-compose up -d --build
```

## 📊 Database Schema

### Core Tables
- `users` - User accounts with authentication
- `exercises` - Exercise catalog (300+ exercises)
- `workout_sessions` - Workout sessions with status tracking
- `workout_exercises` - Junction table linking sessions to exercises
- `strength_sets` - Strength training sets (reps, weight)
- `cardio_sets` - Cardio sets (duration, distance)
- `flexibility_sets` - Flexibility sets (duration, intensity)

All tables include audit fields:
- `created_at` / `updated_at` - Timestamps
- `created_by` / `updated_by` - User tracking
- `version` - Optimistic locking

## 🎯 Project Status

### Completed ✅
- [x] Complete RESTful API (42 endpoints)
- [x] User authentication & authorization
- [x] All CRUD operations
- [x] Performance optimization (N+1 prevention)
- [x] Comprehensive validation
- [x] Error handling & exception management
- [x] Audit trails
- [x] Optimistic locking
- [x] Docker containerization
- [x] SonarQube integration
- [x] Pagination & sorting
- [x] Smart loading strategies

### Roadmap 🚧
- [ ] JWT token-based authentication (alternative to sessions)
- [ ] Frontend application (React/Vue.js)
- [ ] Mobile app (React Native/Flutter)
- [ ] Analytics dashboard
- [ ] Workout templates library
- [ ] Social features (share workouts, follow users)
- [ ] Nutrition tracking integration
- [ ] Exercise video demonstrations
- [ ] Progress photos & measurements
- [ ] Workout history charts

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Coding Standards
- Follow RESTful best practices
- Write unit tests for new features
- Maintain code coverage above 80%
- Use meaningful variable names
- Add JavaDoc for public methods
- Follow Spring Boot conventions

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

**Asen Georgiev**
- GitHub: [@asennesa](https://github.com/asennesa)

## 🙏 Acknowledgments

- Spring Boot community for comprehensive documentation
- PostgreSQL team for robust database support
- Hibernate team for JPA implementation
- MapStruct for clean object mapping
- SonarQube for code quality tools

---

**Built with ❤️ for fitness enthusiasts and developers alike! 💪🏋️‍♀️**

*Happy Workouts!*
