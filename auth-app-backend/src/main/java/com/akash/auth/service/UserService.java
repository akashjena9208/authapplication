package com.akash.auth.service;
import com.akash.auth.dto.UserDto;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserByEmail(String email);

    UserDto getUserById(String userId);

    Iterable<UserDto> getAllUsers();// I used Iterable to keep the service contract generic and avoid coupling to a specific collection type. It also aligns with Spring Data’s findAll() method
    //List<UserDTO> getAllUsers(); //Why people usually prefer List bcz
    // | Reason          | Explanation                   |
    //| --------------- | ----------------------------- |
    //| REST APIs       | JSON responses usually arrays |
    //| Pagination      | `List` works better           |
    //| Utility methods | size(), get(), stream()       |
    UserDto updateUser(UserDto userDto, String userId);

    void deleteUser(UUID userId);
}

