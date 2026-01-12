package com.akash.auth.security;

import com.akash.auth.helpers.UserHelper;
import com.akash.auth.repository.UserRepository;
import com.akash.auth.util.JwtUtil;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private  final JwtUtil jwtUtil;
    private  final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("incoming request: {}", request.getRequestURI());
        // 1️⃣ Read Authorization header
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            //token extract and validate then authentication create and then inside security context  set it.
            //Extract Token
            String token = header.substring(7);
            try {
                //if access work else  token refresh
                if (!jwtUtil.isAccessToken(token)) {
                    //message pass kar hai---
                    filterChain.doFilter(request, response);
                    return;
                }
                //We find Jwt token now we parse
                Jws<Claims> parse = jwtUtil.parse(token);
                Claims payload = parse.getPayload();
                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUUID(userId);
                userRepository.findById(userUuid).ifPresent(user -> {

                    //check for user enable or not

                    if (user.isEnabled()) {
                        // we get user in database
                        List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        //final line : to set the authentication to security context
                        if (SecurityContextHolder.getContext().getAuthentication() == null)
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

                });


            }
            catch (ExpiredJwtException e)
            {
                request.setAttribute("error", "Token Expired");
            }
            catch (Exception e)
            {
                request.setAttribute("error", "Invalid Token");
            }


        }
        filterChain.doFilter(request, response);
    }
    @Override
    // This method decides whether the filter should be skipped for a given request.
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth");
        // If the request URI starts with "/api/v1/auth",
        // it means the request is for authentication endpoints (like login or register).
        // We don't want to apply the filter here because users won't have a token yet.
        // Returning 'true' tells Spring: "Do NOT run this filter for these requests."
        //- Request: /api/v1/users
        //→ shouldNotFilter returns false → filter runs → token is checked.
        //- Request: /api/v1/auth/login
        //→ shouldNotFilter returns true → filter is skipped → user can log in without a token
    }
}
