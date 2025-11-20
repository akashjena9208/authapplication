package com.akash.auth.service;
import com.akash.auth.dto.UserCreateDTO;
import com.akash.auth.dto.UserDTO;
import com.akash.auth.dto.UserUpdateDTO;
import java.util.UUID;

public interface UserService {

    UserDTO createUser(UserCreateDTO createDTO);

    UserDTO getUserByEmail(String email);

    UserDTO getUserById(UUID id);

    Iterable<UserDTO> getAllUsers();

    UserDTO updateUser(UUID id, UserUpdateDTO updateDTO);

    void deleteUser(UUID id);
}

