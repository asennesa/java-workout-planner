# OAuth2 + JWT Authentication Setup

This document provides comprehensive instructions for setting up OAuth2 authentication with JWT tokens in the Workout Planner application.

## Overview

The application now supports:
- **Traditional JWT Authentication** (username/password)
- **OAuth2 Authentication** (Google, GitHub, Facebook)
- **Hybrid Authentication** (OAuth2 users get JWT tokens)

## Features Implemented

### ✅ OAuth2 Providers Supported
- **Google** - Full profile access
- **GitHub** - User profile and email
- **Facebook** - Profile and email access

### ✅ Security Features
- **JWT Token Generation** for OAuth2 users
- **Secure Cookie Handling** with HttpOnly and Secure flags
- **Automatic User Creation** for new OAuth2 users
- **Email-based User Matching** for existing users
- **Role-based Access Control** maintained
- **Comprehensive Error Handling**

### ✅ Industry Best Practices
- **Stateless Authentication** with JWT
- **Secure Redirect Handling**
- **Proper Error Messages** (user-friendly)
- **Comprehensive Logging**
- **Environment-based Configuration**

## Setup Instructions

### 1. Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.developers.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth2 credentials:
   - Go to "Credentials" → "Create Credentials" → "OAuth2 Client ID"
   - Application type: "Web application"
   - Authorized redirect URIs: `http://localhost:8081/login/oauth2/code/google`
   - For production: `https://yourdomain.com/login/oauth2/code/google`

### 2. GitHub OAuth2 Setup

1. Go to [GitHub Settings](https://github.com/settings/applications/new)
2. Create a new OAuth App:
   - Application name: "Workout Planner"
   - Homepage URL: `http://localhost:3000`
   - Authorization callback URL: `http://localhost:8081/login/oauth2/code/github`

### 3. Facebook OAuth2 Setup

1. Go to [Facebook Developers](https://developers.facebook.com/apps/)
2. Create a new app
3. Add Facebook Login product
4. Configure OAuth settings:
   - Valid OAuth Redirect URIs: `http://localhost:8081/login/oauth2/code/facebook`

### 4. Environment Variables

Set the following environment variables:

```bash
# Google OAuth2
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

# GitHub OAuth2
export GITHUB_CLIENT_ID="your-github-client-id"
export GITHUB_CLIENT_SECRET="your-github-client-secret"

# Facebook OAuth2
export FACEBOOK_CLIENT_ID="your-facebook-client-id"
export FACEBOOK_CLIENT_SECRET="your-facebook-client-secret"
```

### 5. Application Properties

Update `application.properties` with your OAuth2 credentials:

```properties
# OAuth2 Configuration
app.oauth2.frontend-url=http://localhost:3000

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}

# Facebook OAuth2
spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_CLIENT_ID}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET}
```

## API Endpoints

### OAuth2 Login Endpoints

- **Google**: `http://localhost:8081/oauth2/authorization/google`
- **GitHub**: `http://localhost:8081/oauth2/authorization/github`
- **Facebook**: `http://localhost:8081/oauth2/authorization/facebook`

### OAuth2 Callback Endpoints

- **Google**: `http://localhost:8081/login/oauth2/code/google`
- **GitHub**: `http://localhost:8081/login/oauth2/code/github`
- **Facebook**: `http://localhost:8081/login/oauth2/code/facebook`

## Frontend Integration

### OAuth2 Login Flow

1. **Redirect to OAuth2 Provider**:
   ```javascript
   window.location.href = 'http://localhost:8081/oauth2/authorization/google';
   ```

2. **Handle Success Redirect**:
   ```javascript
   // The user will be redirected to:
   // http://localhost:3000/auth/oauth2/success?token=JWT_TOKEN
   
   const urlParams = new URLSearchParams(window.location.search);
   const token = urlParams.get('token');
   
   if (token) {
       localStorage.setItem('access_token', token);
       // User is now authenticated
   }
   ```

3. **Handle Error Redirect**:
   ```javascript
   // The user will be redirected to:
   // http://localhost:3000/auth/oauth2/error?code=ERROR_CODE&message=ERROR_MESSAGE
   
   const urlParams = new URLSearchParams(window.location.search);
   const errorCode = urlParams.get('code');
   const errorMessage = urlParams.get('message');
   
   // Display error message to user
   ```

### JWT Token Usage

After successful OAuth2 authentication, the user receives a JWT token that can be used for API calls:

```javascript
// Include JWT token in API requests
const token = localStorage.getItem('access_token');
fetch('/api/v1/users/profile', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});
```

## User Management

### Automatic User Creation

- **New OAuth2 users** are automatically created in the database
- **Username generation**: Based on first name or email prefix
- **Default role**: USER role assigned to all OAuth2 users
- **Email matching**: Existing users are matched by email

### User Data Mapping

| Provider | ID Field | Email | First Name | Last Name | Picture |
|----------|----------|-------|------------|-----------|---------|
| Google | `sub` | `email` | `given_name` | `family_name` | `picture` |
| GitHub | `id` | `email` | `name` (split) | `name` (split) | `avatar_url` |
| Facebook | `id` | `email` | `first_name` | `last_name` | `picture` |

## Security Considerations

### ✅ Implemented Security Measures

1. **Secure Cookies**: HttpOnly, Secure, SameSite=Strict
2. **JWT Expiration**: Configurable token expiration
3. **HTTPS Enforcement**: Secure cookies only work over HTTPS in production
4. **CSRF Protection**: OAuth2 endpoints are CSRF-exempt
5. **Error Handling**: No sensitive information exposed in error messages

### 🔒 Production Security Checklist

- [ ] Use HTTPS in production
- [ ] Set secure JWT secret (256-bit minimum)
- [ ] Configure proper CORS settings
- [ ] Set up rate limiting
- [ ] Monitor authentication logs
- [ ] Regular security audits

## Testing

### Manual Testing

1. **Test Google OAuth2**:
   ```bash
   curl -I http://localhost:8081/oauth2/authorization/google
   ```

2. **Test GitHub OAuth2**:
   ```bash
   curl -I http://localhost:8081/oauth2/authorization/github
   ```

3. **Test Facebook OAuth2**:
   ```bash
   curl -I http://localhost:8081/oauth2/authorization/facebook
   ```

### Integration Testing

The application includes comprehensive error handling and logging for OAuth2 authentication flows.

## Troubleshooting

### Common Issues

1. **Invalid Redirect URI**: Ensure redirect URIs match exactly in OAuth2 provider settings
2. **Missing Scopes**: Check that required scopes are requested (email, profile)
3. **Client ID/Secret**: Verify environment variables are set correctly
4. **CORS Issues**: Configure CORS for frontend domain

### Debug Logging

Enable debug logging for OAuth2:

```properties
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.com.workoutplanner.workoutplanner.service.OAuth2UserService=DEBUG
```

## Architecture

### Components

1. **OAuth2UserService**: Handles OAuth2 user mapping and creation
2. **OAuth2AuthenticationSuccessHandler**: Generates JWT tokens for OAuth2 users
3. **OAuth2AuthenticationFailureHandler**: Handles OAuth2 authentication errors
4. **SecurityConfig**: Configures OAuth2 integration with Spring Security
5. **OAuth2Config**: Centralizes OAuth2 configuration

### Flow Diagram

```
User → OAuth2 Provider → OAuth2UserService → JWT Generation → Frontend Redirect
```

## Next Steps

1. **Configure OAuth2 providers** with your credentials
2. **Set up frontend integration** for OAuth2 login
3. **Test authentication flows** with all providers
4. **Deploy to production** with HTTPS
5. **Monitor authentication logs** for security

## Support

For issues or questions regarding OAuth2 setup, please refer to:
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [GitHub OAuth2 Documentation](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [Facebook OAuth2 Documentation](https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow)
