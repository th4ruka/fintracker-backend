package com.ceylabs.fintrackerbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        logger.info("=== JWT Configuration Initialized ===");
        logSecretInfo();
        logger.info("Access Token Expiration: {} ms ({} min)", accessTokenExpiration, accessTokenExpiration / 60000);
        logger.info("Refresh Token Expiration: {} ms ({} days)", refreshTokenExpiration, refreshTokenExpiration / 86400000);
        logger.info("=====================================");
    }

    private void logSecretInfo() {
        if (secret != null) {
            String maskedSecret = secret.length() > 20
                ? secret.substring(0, 10) + "..." + secret.substring(secret.length() - 10)
                : "***";
            logger.info("JWT Secret loaded - Length: {}, Masked: {}", secret.length(), maskedSecret);
        } else {
            logger.error("JWT Secret is NULL!");
        }
    }

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 15 minutes in milliseconds

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 7 days in milliseconds

    private SecretKey getSigningKey() {
        if (secret == null || secret.isEmpty()) {
            logger.error("JWT secret is not configured!");
            throw new IllegalArgumentException("JWT secret must be configured via jwt.secret property");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        logger.debug("Extracting claims from token - Token length: {}", token.length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            logger.debug("Claims extracted successfully - Subject: {}", claims.getSubject());
            return claims;
        } catch (SignatureException e) {
            logger.error("SIGNATURE VERIFICATION FAILED! This means the JWT_SECRET used for validation is different from the one used for generation.");
            logSecretInfo();
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateAccessToken(UserDetails userDetails) {
        logger.info("Generating access token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername(), accessTokenExpiration);
        logger.info("Access token generated successfully");
        return token;
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            logger.info("Validating token for user: {}", userDetails.getUsername());
            logSecretInfo(); // Log the secret being used for validation

            final String username = extractUsername(token);
            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);

            logger.info("Token validation - Username matches: {}, Not expired: {}", usernameMatches, notExpired);

            if (!usernameMatches) {
                logger.warn("Token username '{}' does not match user details username '{}'", username, userDetails.getUsername());
            }
            if (isTokenExpired(token)) {
                logger.warn("Token has expired for user: {}", username);
            }

            return usernameMatches && notExpired;
        } catch (Exception e) {
            logger.error("Error during token validation: {}", e.getMessage(), e);
            logSecretInfo(); // Also log the secret when there's an error
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            boolean notExpired = !isTokenExpired(token);
            logger.info("Token validation (simple) - Not expired: {}", notExpired);
            return notExpired;
        } catch (Exception e) {
            logger.error("Error during token validation: {}", e.getMessage(), e);
            return false;
        }
    }
}
