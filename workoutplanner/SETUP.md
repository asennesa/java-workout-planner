# Development Setup Guide

## Environment Variables with direnv

This project uses [direnv](https://direnv.net/) to automatically load environment variables when you enter the project directory.

### Initial Setup

1. **direnv is already installed** ✅
2. **Your shell is configured** ✅ (check `~/.zshrc` for the direnv hook)
3. **The `.envrc` file is configured** ✅

### Usage

Simply navigate to the project directory:

```bash
cd workoutplanner
# direnv will automatically load environment variables from .envrc
```

You should see:
```
direnv: loading ~/repos/java_workout_app_repo/java-workout-planner/workoutplanner/.envrc
direnv: export +DB_PASSWORD +DB_USERNAME +POSTGRES_DB +POSTGRES_PASSWORD +POSTGRES_USER ...
```

### Running the Application

With direnv, you can now run Maven commands directly without any wrapper scripts:

```bash
# Start the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Clean and install
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests
```

### Configuring Your Own Credentials

1. Edit the `.envrc` file:
```bash
nano .envrc
```

2. Update the credentials:
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

3. Allow direnv to reload:
```bash
direnv allow
```

**Note:** The `.envrc` file is gitignored and will never be committed to the repository.

### Troubleshooting

If environment variables aren't loading:

```bash
# Check direnv status
direnv status

# Reload the configuration
direnv allow

# Check if variables are set
echo $DB_USERNAME
```

### What Happened to test-env.sh?

The `test-env.sh` script is **no longer needed**! direnv automatically loads environment variables when you enter the directory, making the manual script obsolete.

### Security

- ✅ Credentials are stored in `.envrc` (gitignored)
- ✅ Never committed to the repository
- ✅ Automatically loaded when entering the directory
- ✅ Automatically unloaded when leaving the directory

