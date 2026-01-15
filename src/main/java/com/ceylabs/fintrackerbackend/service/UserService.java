
package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.UserCreateRequest;
import com.ceylabs.fintrackerbackend.dto.UserResponse;
import com.ceylabs.fintrackerbackend.dto.UserUpdateRequest;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Layer
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getUsers(){
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public UserResponse createUser(UserCreateRequest request) {
        Optional<User> userByEmail = userRepository.findUserByEmail(request.getEmail());
        if (userByEmail.isPresent()){
            throw new IllegalStateException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public void deleteUser(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if(!exists){
            throw new IllegalStateException("User ID: "+ userId + " does not exist.");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User with id: " + userId + " doesn't exist."));

        if(request.getName() != null && !request.getName().isEmpty()) {
            existingUser.setName(request.getName());
        }

        if(request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<User> userWithEmail = userRepository.findUserByEmail(request.getEmail());
            if(userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)){
                throw new IllegalStateException("Email already exists.");
            }
            existingUser.setEmail(request.getEmail());
        }

        if(request.getDob() != null) {
            existingUser.setDob(request.getDob());
        }

        User savedUser = userRepository.save(existingUser);
        return mapToResponse(savedUser);
    }

    public UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setDob(user.getDob());

        if (user.getDob() != null) {
            response.setAge(Period.between(user.getDob(), LocalDate.now()).getYears());
        } else {
            response.setAge(0);
        }

        return response;
    }
}
