# Security Setup for SonarQube

## Overview
This document explains how to securely configure SonarQube with PostgreSQL using environment variables instead of hardcoded credentials.

## Security Improvements Made

### 1. Environment Variables
- **Before**: Hardcoded credentials in `docker-compose.yml`
- **After**: Environment variables with fallback defaults
- **Benefit**: Credentials are not exposed in version control

### 2. Environment File Structure
```
.env                 # Contains actual credentials (NOT committed to git)
.env.example         # Template for other developers
.gitignore          # Ensures .env is never committed
```

### 3. Docker Compose Configuration
The `docker-compose.yml` now uses environment variables:
```yaml
environment:
  - POSTGRES_DB=${POSTGRES_DB:-sonar}
  - POSTGRES_USER=${POSTGRES_USER:-sonar}
  - POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-sonar}
```

## Setup Instructions

### For New Developers
1. **Copy the example file**:
   ```bash
   cp .env.example .env
   ```

2. **Update credentials** in `.env`:
   ```bash
   # Edit .env file with secure passwords
   nano .env
   ```

3. **Start services**:
   ```bash
   docker-compose up -d
   ```

### For Production
1. **Use strong passwords**:
   ```bash
   # Generate secure password
   openssl rand -base64 32
   ```

2. **Set environment variables**:
   ```bash
   export POSTGRES_PASSWORD="your_very_secure_password_here"
   export SONAR_JDBC_PASSWORD="your_very_secure_password_here"
   ```

3. **Or use Docker secrets** (recommended for production):
   ```yaml
   secrets:
     postgres_password:
       external: true
   ```

## Security Best Practices

### 1. Password Requirements
- **Minimum 16 characters**
- **Mix of uppercase, lowercase, numbers, symbols**
- **No dictionary words**
- **Unique per environment**

### 2. Environment Separation
- **Development**: Use `.env` file
- **Staging**: Use environment variables
- **Production**: Use Docker secrets or external secret management

### 3. Access Control
- **Limit database access** to SonarQube container only
- **Use network isolation** (already configured)
- **Regular password rotation**

## Current Configuration

### Default Credentials (Development Only)
- **Database**: `sonar`
- **Username**: `sonar`
- **Password**: `sonar_secure_password_2024`

### Network Security
- **Internal network**: `sonar-network`
- **No external database access**
- **SonarQube only accessible on port 9000**

## Troubleshooting

### If Services Don't Start
1. **Check environment variables**:
   ```bash
   echo $POSTGRES_PASSWORD
   ```

2. **Verify .env file exists**:
   ```bash
   ls -la .env
   ```

3. **Check Docker logs**:
   ```bash
   docker-compose logs postgres
   docker-compose logs sonarqube
   ```

### If Database Connection Fails
1. **Verify credentials match** in both services
2. **Check network connectivity**:
   ```bash
   docker-compose exec sonarqube ping postgres
   ```

## Security Checklist

- [ ] `.env` file exists and contains secure passwords
- [ ] `.env` file is in `.gitignore` (already configured)
- [ ] `.env.example` provides template for other developers
- [ ] Passwords are strong and unique
- [ ] Database is not accessible from outside Docker network
- [ ] SonarQube is only accessible on configured port
- [ ] Regular password rotation is planned

## Next Steps

1. **Update production passwords** before deployment
2. **Set up monitoring** for database access
3. **Configure backup strategy** for SonarQube data
4. **Set up SSL/TLS** for production environments
5. **Implement log monitoring** for security events
