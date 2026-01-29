package com.akash.auth.service.impl;
import com.akash.auth.config.AppConstants;
import com.akash.auth.dto.UserDto;
import com.akash.auth.entity.Role;
import com.akash.auth.repository.RoleRepository;
import com.akash.auth.service.AuthService;
import com.akash.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@AllArgsConstructor
public class AuthServiceImpl  implements AuthService {

    private final UserService userService;
    private  final PasswordEncoder passwordEncoder;


    @Override
    public UserDto registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }
}
