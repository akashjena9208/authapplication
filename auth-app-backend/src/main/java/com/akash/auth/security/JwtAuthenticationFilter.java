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

            String token = header.substring(7);
            try {
                if (!jwtUtil.isAccessToken(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                Jws<Claims> parse = jwtUtil.parse(token);
                Claims payload = parse.getPayload();
                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUUID(userId);
                userRepository.findById(userUuid).ifPresent(user -> {

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
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth");

    }
}
