package com.streaker.service;

import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.controller.user.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(UUID id);

    UserResponseDto createUser(CreateUserDto userDto);
}
