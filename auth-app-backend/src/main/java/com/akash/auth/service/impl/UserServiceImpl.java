package com.akash.auth.service.impl;

import com.akash.auth.dto.UserDto;
import com.akash.auth.entity.User;
import com.akash.auth.entity.enums.Provider;
import com.akash.auth.exception.ResourceNotFoundException;
import com.akash.auth.helpers.UserHelper;
import com.akash.auth.mapper.UserMapper;
import com.akash.auth.repository.UserRepository;
import com.akash.auth.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private  final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        // 1️⃣ Validate input
        if (userDto == null || userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with given email already exists");
        }
        // 2️⃣ DTO → Entity
        User user = UserMapper.toEntity(userDto);
        // 3️⃣ Default provider (safety)
        user.setProvider(
                userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL
        );
        // 4️⃣ Persist
        User savedUser = userRepository.save(user);
        // 5️⃣ Entity → DTO (response-safe)
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given email id")
                );
        return UserMapper.toDto(user);
    }


    @Override
    public UserDto getUserById(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository
                .findById(uId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given id")
                );
        return UserMapper.toDto(user);
    }


    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }


    @Override
    public UserDto updateUser(UserDto userDto, String userId) {

        UUID uId = UserHelper.parseUUID(userId);

        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given id")
                );
        // ❗ email is immutable (as you decided)
        //we are not going to change email id for this project.
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getImage() != null) {
            existingUser.setImage(userDto.getImage());
        }
        if (userDto.getProvider() != null) {
            existingUser.setProvider(userDto.getProvider());
        }
        // TODO: password hashing logic later
        if (userDto.getPassword() != null) {
            existingUser.setPassword(userDto.getPassword());
        }
        existingUser.setEnabled(userDto.isEnable());
        // updatedAt handled automatically by @PreUpdate
        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toDto(updatedUser);
    }


    @Override
    public void deleteUser(UUID userId) {
        UUID uId = UserHelper.parseUUID(String.valueOf(userId));
        User user = userRepository
                .findById(uId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given id")
                );
        userRepository.delete(user);
    }
}

