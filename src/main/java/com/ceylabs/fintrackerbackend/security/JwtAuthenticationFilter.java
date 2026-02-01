package com.ceylabs.fintrackerbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.debug("Processing request: {} {}", request.getMethod(), requestPath);

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            logger.debug("JWT token found in Authorization header. Token length: {}", jwt.length());
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Successfully extracted username: {}", username);
            } catch (Exception e) {
                // Invalid token - continue without authentication
                logger.error("Failed to extract username from JWT: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("No Authorization header or invalid format for request: {}", requestPath);
        }

        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("Loaded user details for username: {}", username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    logger.debug("JWT token validated successfully for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set successfully for user: {}", username);
                } else {
                    logger.error("JWT token validation failed for user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Error during authentication processing: {}", e.getMessage(), e);
            }
        } else {
            if (username == null) {
                logger.debug("Username is null - token not validated");
            } else {
                logger.debug("Authentication already exists - skipping");
            }
        }

        filterChain.doFilter(request, response);
    }
}
