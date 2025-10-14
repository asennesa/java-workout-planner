# 💪 Workout Planner

A comprehensive Spring Boot application for managing workout sessions, exercises, and fitness tracking. This application provides a robust backend API for creating, managing, and tracking various types of workouts including strength training, cardio, and flexibility exercises.

## 🚀 Features

- **User Management**: Complete user registration and authentication system
- **Exercise Catalog**: Comprehensive exercise database with different types (Strength, Cardio, Flexibility)
- **Workout Sessions**: Create and manage workout sessions with multiple exercises
- **Set Tracking**: Track different types of sets (reps/weight for strength, duration/distance for cardio, duration/intensity for flexibility)
- **Progress Tracking**: Monitor workout completion status and session history
- **RESTful API**: Clean, well-documented REST endpoints
- **Database Integration**: PostgreSQL with JPA/Hibernate for robust data persistence
- **Code Quality**: Integrated with SonarQube for code analysis and quality metrics

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.5.6
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Java Version**: 17
- **Code Quality**: SonarQube integration
- **Containerization**: Docker & Docker Compose

### Project Structure
```
workoutplanner/
├── src/main/java/com/workoutplanner/workoutplanner/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/            # Data Transfer Objects
│   │   ├── request/    # Request DTOs
│   │   └── response/   # Response DTOs
│   ├── entity/         # JPA entities
│   ├── enums/          # Enumeration types
│   ├── exception/      # Exception handling
│   ├── mapper/         # MapStruct mappers
│   ├── repository/     # Data repositories
│   ├── service/        # Business logic
│   └── validation/     # Custom validators
├── src/main/resources/
│   └── application.properties
└── src/test/           # Test classes
```

## 📊 Database Schema

The application uses a normalized database design with the following core entities:

- **User**: User accounts and profiles
- **Exercise**: Exercise catalog with different types and difficulty levels
- **WorkoutSession**: Individual workout sessions with status tracking
- **WorkoutExercise**: Junction table linking sessions to exercises
- **StrengthSet/CardioSet/FlexibilitySet**: Polymorphic set tracking for different exercise types

For detailed database schema, see [workout-planner-erd.md](workoutplanner/workout-planner-erd.md)

## 🛠️ Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **PostgreSQL** (via Docker)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/java-workout-planner.git
cd java-workout-planner
```

### 2. Set Up Environment Variables
Create a `.env` file in the `workoutplanner/` directory:
```bash
cd workoutplanner
cp .env.example .env  # If you have an example file
# Or create .env with the following content:
```

```env
# SonarQube Database Configuration
POSTGRES_DB=sonar
POSTGRES_USER=sonar
POSTGRES_PASSWORD=SecurePass123!@#

# SonarQube Configuration
SONAR_JDBC_URL=jdbc:postgresql://postgres:5432/sonar
SONAR_JDBC_USERNAME=sonar
SONAR_JDBC_PASSWORD=SecurePass123!@#

# Workout Planner App Database Configuration
DB_USERNAME=sonar
DB_PASSWORD=SecurePass123!@#
```

### 3. Start Database Services
```bash
cd workoutplanner
export $(cat .env | grep -v '^#' | xargs)
docker-compose up -d
```

### 4. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using the test environment script
./test-env.sh
```

The application will be available at: `http://localhost:8081`

## 🐳 Docker Services

The application includes the following Docker services:

- **PostgreSQL Database**: Running on port 5432
- **SonarQube**: Code quality analysis on port 9000 (admin/admin)

## 📚 API Documentation

### API Versioning

This application uses URL path versioning for API endpoints. The current version is **v1**.

All API endpoints follow the pattern: `/api/v1/{resource}`

For comprehensive API versioning documentation, see [API_VERSIONING.md](workoutplanner/API_VERSIONING.md)

### Core Endpoints

#### Users
- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user information

#### Exercises
- `GET /api/v1/exercises` - Get all exercises (with filtering)
- `GET /api/v1/exercises/{id}` - Get exercise by ID
- `POST /api/v1/exercises` - Create new exercise

#### Workout Sessions
- `POST /api/v1/workouts` - Create workout session
- `GET /api/v1/workouts/{id}` - Get workout session details
- `PUT /api/v1/workouts/{id}/status` - Update workout status

### Example API Usage

#### Create a User
```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Create an Exercise
```bash
curl -X POST http://localhost:8081/api/v1/exercises \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Push-ups",
    "description": "Basic push-up exercise",
    "type": "STRENGTH",
    "targetMuscleGroup": "CHEST",
    "difficultyLevel": "BEGINNER"
  }'
```

## 🧪 Testing

Run tests using Maven:
```bash
./mvnw test
```

Run tests with coverage:
```bash
./mvnw clean test jacoco:report
```

## 📈 Code Quality

The project integrates with SonarQube for code quality analysis:

1. Start SonarQube: `docker-compose up -d`
2. Access SonarQube: `http://localhost:9000` (admin/admin)
3. Run analysis: `./mvnw clean verify sonar:sonar`

## 🔧 Development

### Maven Commands
```bash
# Clean and compile
./mvnw clean compile

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package application
./mvnw clean package

# Generate test coverage
./mvnw jacoco:report
```

### Database Commands
```bash
# Connect to database
docker exec -it workoutplanner-postgres psql -U sonar -d workout_planner

# View all databases
docker exec workoutplanner-postgres psql -U sonar -d sonar -c "\l"
```

## 📋 Project Status

- ✅ **Core Entities**: User, Exercise, WorkoutSession, WorkoutExercise
- ✅ **Set Types**: StrengthSet, CardioSet, FlexibilitySet
- ✅ **REST Controllers**: User and Exercise endpoints
- ✅ **API Versioning**: URL path versioning (v1)
- ✅ **Database Integration**: PostgreSQL with JPA/Hibernate
- ✅ **Validation**: Custom validators and exception handling
- ✅ **Docker Support**: Complete containerization setup
- ✅ **Code Quality**: SonarQube integration

## 🚧 Roadmap

- [ ] **Workout Session Management**: Complete CRUD operations for workout sessions
- [ ] **Authentication & Security**: JWT-based authentication
- [ ] **Frontend Interface**: React/Vue.js frontend application
- [ ] **Mobile App**: React Native or Flutter mobile application
- [ ] **Analytics Dashboard**: Workout progress and statistics
- [ ] **Social Features**: Share workouts and follow other users
- [ ] **Workout Templates**: Pre-built workout routines
- [ ] **Nutrition Tracking**: Integration with nutrition APIs

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -m 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request


## 👥 Authors

- **Asen Georgiev** - Initial work - [GitHub Profile](https://github.com/asennesa)

## 🙏 Acknowledgments

- Spring Boot community for excellent documentation
- PostgreSQL team for robust database support
- SonarQube for code quality tools
- MapStruct for object mapping

---

**Happy Workouts! 💪🏋️‍♀️**
