package com.akash.auth.controller;

import com.akash.auth.dto.LoginRequest;
import com.akash.auth.dto.RefreshTokenRequest;
import com.akash.auth.dto.TokenResponse;
import com.akash.auth.dto.UserDto;
import com.akash.auth.entity.RefreshToken;
import com.akash.auth.entity.User;
import com.akash.auth.mapper.UserMapper;
import com.akash.auth.repository.RefreshTokenRepository;
import com.akash.auth.repository.UserRepository;
import com.akash.auth.security.CookieService;
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
import java.util.Arrays;
import java.util.Optional;
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
    private final CookieService cookieService;



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

        // use cookie service to attach refresh token in cookie
        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtUtil.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

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



    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws InterruptedException {


        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));


        if(!jwtUtil.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        String jti = jwtUtil.getJti(refreshToken);
        UUID userId = jwtUtil.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));

        if(storedRefreshToken.isRevoked()){
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        if(storedRefreshToken.getExpiresAt().isBefore(Instant.now())){
            throw new BadCredentialsException("Refresh token expired");
        }

        if(!storedRefreshToken.getUser().getId().equals(userId)){
            throw new BadCredentialsException("Refresh token does not belong to this user");
        }

        //refresh token ko rotate:
        storedRefreshToken.setRevoked(true);
        String newJti= UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenOb);
        String newAccessToken= jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user, newRefreshTokenOb.getJti());


        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtUtil.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtUtil.getAccessTtlSeconds(),UserMapper.toDto(user)));

    }

    //this method will read refresh token from request header or body.
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
//            1. prefer reading refresh token from cookie
        if (request.getCookies() != null) {

            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();

            if (fromCookie.isPresent()) {
                return fromCookie;
            }


        }

        // 2 body:
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        //3. custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

        //Authorization = Bearer <token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtUtil.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Optional.empty();

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtUtil.isRefreshToken(token)) {
                    String jti = jwtUtil.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch (JwtException ignored) {
            }
        });

        // Use CookieUtil (same behavior)
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

