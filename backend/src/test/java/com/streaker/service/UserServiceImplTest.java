package com.streaker.service;

import com.streaker.controller.user.dto.UserDto;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("john", result.getFirst().getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUserDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto dto = userService.getUserById(userId);

        assertEquals("john", dto.getUsername());
        assertEquals(userId, dto.getUuid());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
    }

    @Test
    void createUser_shouldSaveAndReturnDto() {
        UserDto inputDto = new UserDto(new UUID(99L, 1L), "john", "john@example.com");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUuid(userId);
            return u;
        });

        UserDto result = userService.createUser(inputDto);

        assertEquals("john", result.getUsername());
        assertEquals(userId, result.getUuid());
        verify(userRepository).save(any(User.class));
    }
}
