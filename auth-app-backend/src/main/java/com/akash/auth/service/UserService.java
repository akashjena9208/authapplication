package com.akash.auth.service;
import com.akash.auth.dto.UserDto;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserDto userDto);
    UserDto getUserByEmail(String email);
    UserDto getUserById(String userId);
    Iterable<UserDto> getAllUsers();
    UserDto updateUser(UserDto userDto, String userId);
    void deleteUser(UUID userId);
}

