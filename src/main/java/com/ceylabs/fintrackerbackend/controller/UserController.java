package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.UserCreateRequest;
import com.ceylabs.fintrackerbackend.dto.UserResponse;
import com.ceylabs.fintrackerbackend.dto.UserUpdateRequest;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current authenticated user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Optional<User> user = userService.getUserById(userDetails.getId());
        return user.map(userService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get user by ID (only accessible if the ID matches the authenticated user)
     * @deprecated Use /me endpoint instead
     */
    @Deprecated
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Security check: users can only access their own profile
        if (!userDetails.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<User> user = userService.getUserById(id);
        return user.map(userService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create user without authentication
     * @deprecated Use /api/auth/register instead
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<UserResponse> registerNewUser(@Valid @RequestBody UserCreateRequest request){
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete current authenticated user's account
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Update current authenticated user's profile
     */
    @PutMapping
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request){
        UserResponse response = userService.updateUser(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

}
