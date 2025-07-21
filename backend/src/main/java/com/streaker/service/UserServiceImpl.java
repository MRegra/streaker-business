package com.streaker.service;

import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.controller.user.dto.UserResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponseDto createUser(CreateUserDto userDto) {
        User user = new User();
        user.setUsername(userDto.username());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setEmail(userDto.email());
        return mapToDto(userRepository.save(user));
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(user.getUuid(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
