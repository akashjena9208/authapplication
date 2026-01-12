package com.akash.auth.controller;

import com.akash.auth.dto.LoginRequest;
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
}

/*
1️⃣ AuthController → ORCHESTRATOR (API Layer)

📍 Location

controllers/AuthController


📌 What YOU do here

Expose REST APIs

Call services & repositories

Handle HTTP request / response

Attach cookies

Return tokens

❌ What you do NOT do

No password validation logic

No JWT parsing logic

No cookie creation logic

2️⃣ /login → LOGIN FLOW
📌 You do 5 things only
✅ 1. Authenticate credentials
authenticate(loginRequest);


➡️ AuthenticationManager checks email + password
➡️ Uses UserDetailsService internally

✅ 2. Load user from DB
userRepository.findByEmail(...)


➡️ Needed for:

user status

token generation

refresh token mapping

✅ 3. Create & store refresh token (DB)
RefreshToken.builder()


➡️ Purpose:

Track sessions

Support logout

Enable rotation

📍 Stored in refresh_tokens table

✅ 4. Generate JWTs
jwtService.generateAccessToken(user);
jwtService.generateRefreshToken(user, jti);

Token	Used for
Access Token	API authorization
Refresh Token	Renew access token
✅ 5. Attach refresh token to cookie
cookieService.attachRefreshCookie(...)


➡️ HTTP-only
➡️ Secure from JS access
➡️ Long lived

3️⃣ /refresh → TOKEN RENEWAL FLOW
📌 You are doing ADVANCED JWT DESIGN here 👏
✅ Step 1. Read refresh token
readRefreshTokenFromRequest()


Priority order:

Cookie (BEST)

Request body

Custom header

Authorization header

✅ Step 2. Validate token TYPE
jwtService.isRefreshToken(token)


➡️ Prevents access token misuse

✅ Step 3. Validate DB refresh token

You check:

Exists

Not revoked

Not expired

Belongs to same user

📌 This blocks stolen token reuse

✅ Step 4. Rotate refresh token
oldToken.setRevoked(true);
newToken = createNewRefreshToken();


➡️ Old token is dead
➡️ New token replaces it
➡️ Strong security feature

✅ Step 5. Issue new tokens

New access token

New refresh token

Cookie updated

4️⃣ /logout → SESSION TERMINATION
📌 You do 3 things

Read refresh token

Revoke it in DB

Clear cookie

➡️ User is fully logged out
➡️ Token cannot be reused

5️⃣ JwtService → JWT BRAIN

📍 security/JwtService

📌 Responsible for:

Token creation

Claims

Token type identification

TTL

Parsing & validation

❌ Controller never parses JWT directly

6️⃣ CookieService → COOKIE HANDLER

📍 security/CookieService

📌 Handles:

HTTP-only cookie creation

Secure flags

Clearing cookies

Cache headers

➡️ Keeps controller clean

7️⃣ RefreshTokenRepository → SESSION STORE

📍 repositories/RefreshTokenRepository

📌 Handles:

Save refresh token

Find by JTI

Revoke tokens

8️⃣ WHAT YOU HAVE NOT DONE YET (IMPORTANT)
❗ Missing piece
👉 JwtAuthenticationFilter

This filter will:

Read ACCESS TOKEN

Validate it

Set SecurityContext

Without it:
❌ Secured APIs won’t work

🧠 SUMMARY (ONE LINE EACH)
Component	Your Responsibility
Controller	API orchestration
AuthenticationManager	Credential check
JwtService	Token logic
CookieService	Cookie logic
RefreshToken DB	Session tracking
Refresh endpoint	Token rotation
Logout	Token revocation
*/