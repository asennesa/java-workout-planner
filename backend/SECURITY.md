# Security Documentation

This document describes the security architecture and measures implemented in the Workout Planner API.

## Table of Contents

- [Authentication](#authentication)
- [Authorization](#authorization)
- [Security Headers](#security-headers)
- [Rate Limiting](#rate-limiting)
- [Input Validation](#input-validation)
- [Logging & Monitoring](#logging--monitoring)
- [Vulnerability Reporting](#vulnerability-reporting)

---

## Authentication

### Auth0 OAuth2 + JWT

The API uses [Auth0](https://auth0.com/) as the identity provider with JWT (JSON Web Token) authentication.

**Configuration:**
- **Algorithm:** RS256 (RSA Signature with SHA-256)
- **Token Validation:**
  - Issuer URI validation
  - Audience validation
  - Signature verification via Auth0's JWKS endpoint
- **Session Management:** Stateless (no server-side sessions)

**Key Files:**
- `config/Auth0SecurityConfig.java` - Security filter chain configuration
- `security/Auth0JwtAuthenticationConverter.java` - JWT to authentication conversion
- `security/Auth0UserSyncFilter.java` - User synchronization and email verification

### Email Verification

Users must verify their email address through Auth0 before accessing protected resources. Unverified users receive a 403 Forbidden response.

---

## Authorization

### Three-Layer Authorization Model

1. **Authentication:** Valid JWT token from Auth0
2. **Permission-Based (OAuth2 Scopes):** `hasAuthority('read:workouts')`, etc.
3. **Ownership-Based:** User owns the specific resource

### OAuth2 Scopes/Permissions

| Permission | Description |
|------------|-------------|
| `read:workouts` | Read workout sessions |
| `write:workouts` | Create/update workout sessions |
| `delete:workouts` | Delete workout sessions |
| `read:exercises` | Read exercises |
| `write:exercises` | Create/update exercises |
| `delete:exercises` | Delete exercises |
| `read:users` | Admin: Read all users |
| `delete:users` | Admin: Delete users |

### Resource Ownership

The `ResourceSecurityService` enforces that users can only access their own resources:

```java
@PreAuthorize("@resourceSecurityService.canAccessWorkout(#sessionId)")
public WorkoutResponse getWorkoutSessionById(Long sessionId) {
    // Only accessible if user owns this workout or is admin
}
```

**Key Files:**
- `service/ResourceSecurityService.java` - Ownership validation
- Service classes with `@PreAuthorize` annotations

---

## Security Headers

All responses include comprehensive security headers following [OWASP Secure Headers](https://owasp.org/www-project-secure-headers/) guidelines.

| Header | Value | Purpose |
|--------|-------|---------|
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | Force HTTPS |
| `Content-Security-Policy` | Restrictive policy | Prevent XSS |
| `X-Content-Type-Options` | `nosniff` | Prevent MIME sniffing |
| `X-Frame-Options` | `DENY` | Prevent clickjacking |
| `X-XSS-Protection` | `1; mode=block` | Legacy XSS protection |
| `Referrer-Policy` | `no-referrer` | Prevent referrer leakage |
| `Permissions-Policy` | Restrictive policy | Disable browser features |
| `Cross-Origin-Opener-Policy` | `same-origin` | Isolate browsing context |
| `Cross-Origin-Resource-Policy` | `same-origin` | Restrict resource loading |
| `X-Permitted-Cross-Domain-Policies` | `none` | Block cross-domain policies |

**Key Files:**
- `config/Auth0SecurityConfig.java` - Spring Security headers
- `security/SecurityHeadersFilter.java` - Custom headers filter

---

## Rate Limiting

Rate limiting is implemented using [Bucket4j](https://github.com/bucket4j/bucket4j) with the token bucket algorithm.

### Configured Limits

| Endpoint | Limit | Period | Purpose |
|----------|-------|--------|---------|
| `POST /api/v1/users` | 10 | 1 hour | Prevent mass account creation |
| `GET /api/v1/users/check-username` | 10 | 1 minute | Prevent user enumeration |
| `GET /api/v1/users/check-email` | 10 | 1 minute | Prevent user enumeration |
| `DELETE /api/v1/users/*` | 10 | 1 hour | Protect against mass deletion |
| `POST /api/v1/workouts` | 100 | 1 minute | Standard API protection |

### Anti-Enumeration Measures

The check-username and check-email endpoints include:
- Strict rate limiting (10 requests/minute)
- Artificial timing delay (50-150ms random) to prevent timing attacks

**Key Files:**
- `application-ratelimit.yml` - Rate limit configuration
- `controller/UserController.java` - Anti-timing delay implementation

---

## Input Validation

### Jakarta Bean Validation

All DTOs use Jakarta Bean Validation annotations:

```java
public class CreateWorkoutRequest {
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
```

### SQL Injection Prevention

- All database queries use JPA parameterized queries
- No raw SQL string concatenation

### Request Size Limits

To prevent DoS attacks via large payloads:

| Setting | Value |
|---------|-------|
| Max file upload | 2MB |
| Max request size | 2MB |
| Max form post size | 2MB |
| Max header size | 16KB |

**Key Files:**
- `dto/request/*.java` - Validated DTOs
- `application.properties` - Size limits
- `exception/ValidationExceptionHandler.java` - Error responses

---

## Logging & Monitoring

### Security Audit Logging

Security-relevant events are logged to a dedicated audit log file (`logs/security-audit.log`) with structured format:

**Logged Events:**
- Authentication success/failure
- Authorization denied
- Rate limit exceeded
- Suspicious activity
- Resource access (audit trail)

**Log Format:**
```
event=AUTHORIZATION_DENIED userId=123 resourceType=WORKOUT resourceId=456 action=READ correlationId=abc-123
```

### Log Configuration

- **Development:** Console + file logging, DEBUG level
- **Production:** Console + file + JSON + security audit, INFO level
- **Retention:** 90 days for security audit logs

**Key Files:**
- `security/SecurityEventLogger.java` - Structured security logging
- `logback-spring.xml` - Logging configuration

---

## Vulnerability Reporting

### security.txt

A vulnerability disclosure policy is available at:
```
/.well-known/security.txt
```

### Reporting Process

1. **Email:** security@workoutplanner.example.com (configure for your domain)
2. **Include:**
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Your contact information
3. **Response Time:** Within 5 business days

### Scope

In-scope:
- Authentication/authorization bypass
- Data exposure
- Injection vulnerabilities
- Cross-site scripting (XSS)
- Cross-site request forgery (CSRF)

Out-of-scope:
- Denial of service attacks
- Social engineering
- Physical security

---

## API Security Best Practices

### For API Consumers

1. **Store tokens securely** - Never expose JWT tokens in URLs or logs
2. **Use HTTPS only** - The API enforces HTTPS via HSTS
3. **Handle token expiration** - Implement token refresh logic
4. **Validate responses** - Don't trust API responses blindly

### CORS Configuration

CORS is configured to allow only specific origins. Configure allowed origins via environment variables:
- `CORS_ALLOWED_ORIGINS` - Comma-separated list of allowed origins

---

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Auth0 Spring Security Integration](https://auth0.com/docs/quickstart/backend/java-spring-security5)
- [OWASP Secure Headers](https://owasp.org/www-project-secure-headers/)
