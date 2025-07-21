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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    void getAllUsers_shouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("john", result.getFirst().username());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUserDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDto dto = userService.getUserById(userId);

        assertEquals("john", dto.username());
        assertEquals(userId, dto.uuid());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void createUser_shouldSaveAndReturnDto() {
        CreateUserDto inputDto = new CreateUserDto("john", "john@example.com", "password12345");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUuid(userId);
            return u;
        });

        UserResponseDto result = userService.createUser(inputDto);

        assertEquals("john", result.username());
        assertEquals(userId, result.uuid());
        verify(userRepository).save(any(User.class));
    }
}
