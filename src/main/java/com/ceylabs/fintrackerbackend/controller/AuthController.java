package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.AuthResponse;
import com.ceylabs.fintrackerbackend.dto.LoginRequest;
import com.ceylabs.fintrackerbackend.dto.RefreshTokenRequest;
import com.ceylabs.fintrackerbackend.dto.RegisterRequest;
import com.ceylabs.fintrackerbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Handles user authentication including registration, login, and token refresh.
 * All endpoints are public and do not require authentication.
 *
 * TODO: Add OAuth2 support for social login (Google, GitHub, etc.)
 * TODO: Implement password reset functionality
 * TODO: Add email verification for new registrations
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user account
     *
     * @param request Registration details (name, email, password, dob)
     * @return JWT tokens and user information
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login with email and password
     *
     * @param request Login credentials (email, password)
     * @return JWT tokens and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token
     *
     * @param request Refresh token
     * @return New JWT tokens and user information
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout and revoke refresh token
     *
     * @param request Refresh token to revoke
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
