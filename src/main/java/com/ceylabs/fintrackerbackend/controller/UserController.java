package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.UserCreateRequest;
import com.ceylabs.fintrackerbackend.dto.UserResponse;
import com.ceylabs.fintrackerbackend.dto.UserUpdateRequest;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(){
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(userService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerNewUser(@Valid @RequestBody UserCreateRequest request){
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path="{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path="{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateRequest request){
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

}
