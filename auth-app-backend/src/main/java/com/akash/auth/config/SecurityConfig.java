package com.akash.auth.config;

import com.akash.auth.dto.error.ApiError;
import com.akash.auth.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@AllArgsConstructor
@Configuration
@EnableMethodSecurity(prePostEnabled = true) //method level security
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

//                .authorizeHttpRequests(auth -> auth
//                        // Public APIs
//                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/logout").permitAll()
//
//                        // Everything else requires authentication
//                        .anyRequest().authenticated()
//                )
//
//                )
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests.requestMatchers(AppConstants.AUTH_PUBLIC_URLS).permitAll()
                                .requestMatchers(AppConstants.AUTH_ADMIN_URLS).hasRole(AppConstants.ADMIN_ROLE)
                                .requestMatchers(AppConstants.AUTH_GUEST_URLS).hasRole(AppConstants.GUEST_ROLE)
//                                .requestMatchers(HttpMethod.GET).hasRole(AppConstants.GUEST_ROLE)
//                                .requestMatchers("api/v1/users/**").hasRole(AppConstants.ADMIN_ROLE)
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(successHandler)
                                .failureHandler(null)
                )
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {
                                    response.setStatus(401);
                                    response.setContentType("application/json");
                                    String message = e.getMessage();
                                    String error = (String) request.getAttribute("error");
                                    if (error != null) {
                                        message = error;
                                    }
                                    var apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access", message, request.getRequestURI(), true);
                                    var objectMapper = new ObjectMapper();
                                    response.getWriter().write(objectMapper.writeValueAsString(apiError));
                                })
                                //When Token is Correct Access not Given that time
                                .accessDeniedHandler((request, response, e) -> {

                                    response.setStatus(403);
                                    response.setContentType("application/json");
                                    String message = e.getMessage();
                                    String error = (String) request.getAttribute("error");
                                    if (error != null) {
                                        message = error;
                                    }
                                    var apiError = ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden Access", message, request.getRequestURI(), true);
                                    var objectMapper = new ObjectMapper();
                                    response.getWriter().write(objectMapper.writeValueAsString(apiError));


                                })

//
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }


}
