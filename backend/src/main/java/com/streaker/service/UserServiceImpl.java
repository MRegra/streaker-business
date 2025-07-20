package com.streaker.service;

import com.streaker.controller.user.dto.UserDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        return mapToDto(userRepository.save(user));
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user.getUuid(), user.getUsername(), user.getEmail());
    }
}
