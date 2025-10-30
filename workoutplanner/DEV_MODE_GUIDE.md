# Development Mode Guide

## 🚀 Quick Start - Disable Security for Easy Testing

### Option 1: Run with IntelliJ IDEA (Recommended)

1. **Edit Run Configuration:**
   - Go to `Run` → `Edit Configurations`
   - Select your Spring Boot application
   - In `Environment variables`, add: `SPRING_PROFILES_ACTIVE=dev`
   - Or in `VM options`, add: `-Dspring.profiles.active=dev`
   - Click `Apply` and `OK`

2. **Run the application**

### Option 2: Run with Maven

```bash
cd workoutplanner
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 3: Run with JAR

```bash
java -jar -Dspring.profiles.active=dev target/workoutplanner-0.0.1-SNAPSHOT.jar
```

### Option 4: Set Environment Variable (IntelliJ Terminal)

```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

---

## ✅ Verify Dev Mode is Active

When dev mode is active, you'll see in the logs:

```
[DEV MODE] Security: DevSecurityConfig active - ALL requests permitted
```

You should also see:
```
Active Profiles: dev
```

---

## 🧪 Testing Your API Without Authentication

### Using cURL

```bash
# Create a user - NO AUTHENTICATION NEEDED
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'

# Get all users - NO AUTHENTICATION NEEDED
curl http://localhost:8081/api/v1/users

# Get user by ID - NO AUTHENTICATION NEEDED
curl http://localhost:8081/api/v1/users/1
```

### Using Postman

1. Create a new request
2. Set URL: `http://localhost:8081/api/v1/users`
3. **No need to set Authorization header**
4. Send request!

### Using VS Code REST Client

Create a file `test.http`:

```http
### Create User (Dev Mode - No Auth)
POST http://localhost:8081/api/v1/users
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User"
}

### Get All Users (Dev Mode - No Auth)
GET http://localhost:8081/api/v1/users
```

---

## 🔒 Switch Back to Production Security

### Option 1: Remove the Profile

Simply remove the `-Dspring.profiles.active=dev` parameter or environment variable.

### Option 2: Explicitly Set Production Profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Or in IntelliJ:
- Set `SPRING_PROFILES_ACTIVE=prod`

---

## ⚠️ Important Notes

### What Dev Mode Does:

✅ **Allows all requests without authentication**
- No need for username/password
- No need for Authorization headers
- All endpoints are publicly accessible

✅ **Maintains other development features**
- Verbose logging (DEBUG level)
- SQL query logging
- Detailed error messages
- All actuator endpoints exposed

✅ **Safe for development**
- Only active when you explicitly enable 'dev' profile
- Production security (SecurityConfig) takes over by default

### What Dev Mode Does NOT Do:

❌ **Does NOT disable validation**
- Request body validation still works
- You still need valid JSON payloads

❌ **Does NOT disable CORS**
- CORS is still active (but relaxed for dev)
- Allows localhost origins

❌ **Does NOT disable the database**
- You still need PostgreSQL running
- Database credentials still required

---

## 🧪 Testing with Authentication (Production Mode)

If you want to test with security enabled:

### Using cURL with HTTP Basic Auth

```bash
# First, create a user in dev mode, then switch to prod mode

# Login/Authenticate (prod mode)
curl -X POST http://localhost:8081/api/v1/auth/login \
  -u "testuser:password123"

# Or include credentials in every request
curl http://localhost:8081/api/v1/users \
  -u "testuser:password123"
```

### Using Postman with HTTP Basic Auth

1. Go to `Authorization` tab
2. Select `Basic Auth`
3. Enter username and password
4. Send request

---

## 📊 Comparison Table

| Feature | Dev Mode (`dev` profile) | Production Mode (default) |
|---------|-------------------------|---------------------------|
| Authentication Required | ❌ No | ✅ Yes |
| Authorization Checks | ❌ Disabled | ✅ Enabled |
| Logging Level | 🔊 DEBUG | 🔇 INFO/WARN |
| SQL Logging | ✅ Enabled | ❌ Disabled |
| Actuator Endpoints | 🌐 All Exposed | 🔒 Limited |
| Session Timeout | ⏰ 8 hours | ⏰ 30 minutes |
| CORS | 🌍 Permissive | 🔒 Strict |

---

## 🎯 Best Practices

### During Development:

1. **Use dev mode for quick testing**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Test with security before deploying**
   - Remove dev profile
   - Test authentication flows
   - Verify role-based access control

3. **Never commit with dev profile as default**
   - Keep `application.properties` with production defaults
   - Use dev profile explicitly when needed

### Before Deployment:

1. ✅ Remove or disable dev profile
2. ✅ Test all endpoints with proper authentication
3. ✅ Verify role-based access control works
4. ✅ Check that sensitive endpoints are protected
5. ✅ Review logs for security warnings

---

## 🆘 Troubleshooting

### Problem: Still getting 401 Unauthorized

**Solution:** Verify dev profile is active
```bash
# Check application logs for:
Active Profiles: dev
```

### Problem: App won't start with dev profile

**Solution:** Check for bean conflicts
- Make sure DevSecurityConfig and SecurityConfig both have proper @Profile annotations
- DevSecurityConfig: `@Profile("dev")`
- SecurityConfig: `@Profile("!dev")`

### Problem: Changes not taking effect

**Solution:** Rebuild and restart
```bash
./mvnw clean install
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📝 Summary

**To disable security for easy testing:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Then test freely:**

```bash
curl http://localhost:8081/api/v1/users
# No authentication needed! 🎉
```

**When done, switch back to secure mode:**

```bash
./mvnw spring-boot:run
# Security is back on 🔒
```

---

## 🔗 Related Files

- **DevSecurityConfig.java** - Development security configuration (permits all)
- **SecurityConfig.java** - Production security configuration (requires auth)
- **application-dev.properties** - Development-specific settings
- **application.properties** - Default/production settings

