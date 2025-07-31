package com.streaker.service;

import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.controller.user.dto.UserResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setUuid(userId);
        user.setUsername("john");
        user.setEmail("john@example.com");
    }

    @Test
    void shouldReturnAllUsers_whenUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertAll("All users",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("john", result.getFirst().username()),
                () -> assertEquals(userId, result.getFirst().uuid())
        );

        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnUserById_whenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDto dto = userService.getUserById(userId);

        assertAll("User by ID",
                () -> assertEquals("john", dto.username()),
                () -> assertEquals(userId, dto.uuid())
        );

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowException_whenUserNotFoundById() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldCreateUser_andReturnDto() {
        CreateUserDto input = new CreateUserDto("john", "john@example.com", "password12345");

        when(passwordEncoder.encode(input.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUuid(userId);
            return savedUser;
        });

        UserResponseDto result = userService.createUser(input);

        assertAll("Created user",
                () -> assertEquals("john", result.username()),
                () -> assertEquals(userId, result.uuid())
        );

        verify(passwordEncoder).encode("password12345");
        verify(userRepository).save(any(User.class));
    }
}
