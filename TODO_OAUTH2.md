# OAuth2 Integration TODO

This document outlines the steps needed to add OAuth2 social login support to the financial tracker application.

## Current Authentication Status
- ✅ JWT-based authentication with email/password
- ✅ Access tokens (15 minutes) and refresh tokens (7 days)
- ✅ Secure password hashing with BCrypt
- ✅ Role-based access control foundation
- ✅ All endpoints secured with Spring Security

## OAuth2 Enhancement Plan

### 1. Add OAuth2 Dependencies

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### 2. Configure OAuth2 Providers

Add to `application.yml`:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - user:email
              - read:user
```

### 3. Create OAuth2 User Service

Create `OAuth2UserService.java` to:
- Handle OAuth2 login callbacks
- Create or update user accounts from OAuth2 providers
- Link OAuth2 accounts to existing email/password accounts
- Generate JWT tokens after OAuth2 authentication

### 4. Update User Entity

Add fields to track OAuth2 authentication:
```java
private String provider; // "google", "github", "local"
private String providerId; // OAuth2 provider's user ID
```

### 5. Create Database Migration

Add migration for OAuth2 fields:
```sql
ALTER TABLE user
    ADD COLUMN provider VARCHAR(50) DEFAULT 'local',
    ADD COLUMN provider_id VARCHAR(255),
    ADD INDEX idx_provider_id (provider, provider_id);
```

### 6. Update SecurityConfig

Modify `SecurityConfig.java` to:
- Add OAuth2 login configuration
- Configure OAuth2 success/failure handlers
- Maintain JWT token generation for OAuth2 users

### 7. Frontend Integration

Update frontend to:
- Add "Sign in with Google" button
- Add "Sign in with GitHub" button
- Handle OAuth2 redirect flow
- Store JWT tokens from OAuth2 login

### 8. Testing Considerations

- Test account linking (OAuth2 to existing email account)
- Test first-time OAuth2 registration
- Test token refresh for OAuth2 users
- Test logout with OAuth2 sessions
- Verify CORS configuration for OAuth2 callbacks

## Benefits of OAuth2 Integration

1. **Better User Experience**: One-click sign-in with existing accounts
2. **Reduced Password Fatigue**: Users don't need to remember another password
3. **Enhanced Security**: Leverage provider's MFA and security features
4. **Faster Onboarding**: Quick registration without email verification
5. **Profile Syncing**: Automatic profile picture and name updates

## Security Considerations

- Always validate OAuth2 tokens from providers
- Store minimal OAuth2 data (don't store access tokens)
- Allow users to unlink OAuth2 providers
- Support multiple OAuth2 providers per account
- Implement proper CSRF protection for OAuth2 flows

## Priority Level: Medium

This is a nice-to-have enhancement but not critical for MVP. Email/password authentication is sufficient for initial launch.

## References

- [Spring Security OAuth2 Client Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth2 Setup](https://console.cloud.google.com/apis/credentials)
- [GitHub OAuth Apps](https://github.com/settings/developers)
