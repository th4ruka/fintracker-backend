package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.AuthResponse;
import com.ceylabs.fintrackerbackend.dto.LoginRequest;
import com.ceylabs.fintrackerbackend.dto.RegisterRequest;
import com.ceylabs.fintrackerbackend.model.RefreshToken;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.RefreshTokenRepository;
import com.ceylabs.fintrackerbackend.repository.UserRepository;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDob(request.getDateOfBirth());
        user.setRoles("USER");

        User savedUser = userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = new CustomUserDetails(savedUser);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshTokenString = jwtUtil.generateRefreshToken(savedUser.getEmail());

        // Save refresh token
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenString,
                savedUser,
                LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenString,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Get user from database
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshTokenString = jwtUtil.generateRefreshToken(user.getEmail());

        // Save refresh token
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenString,
                user,
                LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenString,
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenString)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        // Check if expired
        if (refreshToken.isExpired()) {
            throw new IllegalStateException("Refresh token has expired");
        }

        // Get user
        User user = refreshToken.getUser();

        // Generate new access token
        UserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        // Generate new refresh token
        String newRefreshTokenString = jwtUtil.generateRefreshToken(user.getEmail());

        // Revoke old refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Save new refresh token
        RefreshToken newRefreshToken = new RefreshToken(
                newRefreshTokenString,
                user,
                LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(newRefreshToken);

        return new AuthResponse(
                accessToken,
                newRefreshTokenString,
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    @Transactional
    public void logout(String refreshTokenString) {
        refreshTokenRepository.findByToken(refreshTokenString)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
    }
}
