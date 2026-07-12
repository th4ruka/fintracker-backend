package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.UserCreateRequest;
import com.ceylabs.fintrackerbackend.dto.UserResponse;
import com.ceylabs.fintrackerbackend.dto.UserUpdateRequest;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setDob(LocalDate.of(1990, 1, 1));

        createRequest = new UserCreateRequest();
        createRequest.setName("Jane Smith");
        createRequest.setEmail("jane@example.com");
        createRequest.setDob(LocalDate.of(1995, 5, 15));

        updateRequest = new UserUpdateRequest();
    }

    // ==================== getUsers() Tests ====================

    @Test
    @DisplayName("getUsers - Should return all users as UserResponse list")
    void getUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setDob(LocalDate.of(1995, 5, 15));

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // When
        List<UserResponse> result = userService.getUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getUsers - Should return empty list when no users exist")
    void getUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<UserResponse> result = userService.getUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    // ==================== getUserById() Tests ====================

    @Test
    @DisplayName("getUserById - Should return user when user exists")
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("John Doe");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserById - Should return empty optional when user does not exist")
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(999L);
    }

    // ==================== createUser() Tests ====================

    @Test
    @DisplayName("createUser - Should create user successfully with valid data")
    void createUser_WithValidData_ShouldCreateUser() {
        // Given
        User newUser = new User();
        newUser.setId(1L);
        newUser.setName(createRequest.getName());
        newUser.setEmail(createRequest.getEmail());
        newUser.setDob(createRequest.getDob());

        when(userRepository.findUserByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getDob()).isEqualTo(LocalDate.of(1995, 5, 15));
        verify(userRepository, times(1)).findUserByEmail(createRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Should throw exception when email already exists")
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.findUserByEmail(createRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, times(1)).findUserByEmail(createRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Should calculate age correctly when dob is provided")
    void createUser_WithDob_ShouldCalculateAge() {
        // Given
        User newUser = new User();
        newUser.setId(1L);
        newUser.setName(createRequest.getName());
        newUser.setEmail(createRequest.getEmail());
        newUser.setDob(LocalDate.of(1990, 1, 1));

        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result.getAge()).isGreaterThan(0);
        assertThat(result.getDob()).isNotNull();
    }

    // ==================== deleteUser() Tests ====================

    @Test
    @DisplayName("deleteUser - Should delete user when user exists")
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - Should throw exception when user does not exist")
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User ID: 999 does not exist");

        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // ==================== updateUser() Tests ====================

    @Test
    @DisplayName("updateUser - Should update user name successfully")
    void updateUser_WithNewName_ShouldUpdateName() {
        // Given
        updateRequest.setName("John Updated");
        updateRequest.setEmail(null);
        updateRequest.setDateOfBirth(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getName()).isEqualTo("John Updated");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Should update user email when email is not taken")
    void updateUser_WithNewEmail_ShouldUpdateEmail() {
        // Given
        updateRequest.setName(null);
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setDateOfBirth(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findUserByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getEmail()).isEqualTo("newemail@example.com");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findUserByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Should throw exception when new email already exists for another user")
    void updateUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("taken@example.com");

        updateRequest.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findUserByEmail("taken@example.com")).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findUserByEmail("taken@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should allow updating to same email for same user")
    void updateUser_WithSameEmail_ShouldAllowUpdate() {
        // Given
        updateRequest.setEmail("john@example.com"); // Same email as testUser

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findUserByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findUserByEmail("john@example.com");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Should update date of birth successfully")
    void updateUser_WithNewDob_ShouldUpdateDob() {
        // Given
        LocalDate newDob = LocalDate.of(1992, 12, 25);
        updateRequest.setDateOfBirth(newDob);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getDob()).isEqualTo(newDob);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Should throw exception when user does not exist")
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with id: 999 doesn't exist");

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should not update fields when request values are null")
    void updateUser_WithNullValues_ShouldNotUpdateFields() {
        // Given
        String originalName = testUser.getName();
        String originalEmail = testUser.getEmail();
        LocalDate originalDob = testUser.getDob();

        updateRequest.setName(null);
        updateRequest.setEmail(null);
        updateRequest.setDateOfBirth(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(testUser.getName()).isEqualTo(originalName);
        assertThat(testUser.getEmail()).isEqualTo(originalEmail);
        assertThat(testUser.getDob()).isEqualTo(originalDob);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Should not update fields when request values are empty strings")
    void updateUser_WithEmptyStrings_ShouldNotUpdateFields() {
        // Given
        String originalName = testUser.getName();
        String originalEmail = testUser.getEmail();

        updateRequest.setName("");
        updateRequest.setEmail("");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(testUser.getName()).isEqualTo(originalName);
        assertThat(testUser.getEmail()).isEqualTo(originalEmail);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    // ==================== mapToResponse() Tests ====================

    @Test
    @DisplayName("mapToResponse - Should map user entity to response DTO correctly")
    void mapToResponse_ShouldMapCorrectly() {
        // When
        UserResponse result = userService.mapToResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getName()).isEqualTo(testUser.getName());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getDob()).isEqualTo(testUser.getDob());
        assertThat(result.getAge()).isGreaterThan(0);
    }

    @Test
    @DisplayName("mapToResponse - Should handle null date of birth")
    void mapToResponse_WithNullDob_ShouldNotCalculateAge() {
        // Given
        testUser.setDob(null);

        // When
        UserResponse result = userService.mapToResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDob()).isNull();
        assertThat(result.getAge()).isZero();
    }
}
