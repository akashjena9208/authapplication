package com.akash.auth.service.impl;

import com.akash.auth.dto.UserDto;
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
        //logic
        //verify email
        //verify password
        //default roles
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }
}
