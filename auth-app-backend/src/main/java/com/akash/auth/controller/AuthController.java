package com.akash.auth.controller;

import com.akash.auth.dto.LoginRequest;
import com.akash.auth.dto.TokenResponse;
import com.akash.auth.dto.UserDto;
import com.akash.auth.entity.RefreshToken;
import com.akash.auth.entity.User;
import com.akash.auth.mapper.UserMapper;
import com.akash.auth.repository.RefreshTokenRepository;
import com.akash.auth.repository.UserRepository;
import com.akash.auth.service.AuthService;
import com.akash.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager; //this guy check username pw
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;



    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

    //After authenticate then token generate and  username & pw  check  AuthenticationManager we create bean also
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // authenticate
        Authentication authenticate = authenticate(loginRequest);
        // Fetch user
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid Username or Password"));
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        //Create Refresh Token (DB)
        String jti = UUID.randomUUID().toString();
        var newrefreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        //refresh token information save
        refreshTokenRepository.save(newrefreshTokenOb);

        //Generate Access Token
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user, newrefreshTokenOb.getJti());



        //Response
        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtUtil.getAccessTtlSeconds(),UserMapper.toDto(user));
        return ResponseEntity.ok(tokenResponse);

    }

    //Generate Authentication
    private Authentication authenticate(LoginRequest loginRequest) {
        try {

            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Username or Password !!");
        }
    }
}