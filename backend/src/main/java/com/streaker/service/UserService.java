package com.streaker.service;

import com.streaker.controller.user.dto.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(UUID id);

    UserDto createUser(UserDto userDto);
}
