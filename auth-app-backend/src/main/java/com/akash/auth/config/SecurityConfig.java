package com.akash.auth.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(authorizeHttpRequests ->
//                authorizeHttpRequests
//                        .requestMatchers("api/v1/auth/register").permitAll()
//                        .requestMatchers("api/v1/auth/login").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(Customizer.withDefaults())
//                .httpBasic(Customizer.withDefaults());
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public APIs
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Enable default Spring login page (browser)
                .formLogin(Customizer.withDefaults())

                // Enable HTTP Basic (Postman / API clients)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }




    //In Memory
//    @Bean
//    public UserDetailsService users() {
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//        UserDetails user1 = userBuilder.username("akash-jena").password("abc").roles("ADMIN").build();
//        UserDetails user2 = userBuilder.username("subha").password("xyz").roles("ADMIN").build();
//        UserDetails user3 = userBuilder.username("akash").password("").roles("USER").build();
//        return new InMemoryUserDetailsManager(user1, user2, user3);
//    }

}
//Although user data is stored in MySQL, Spring Security only authenticates via a UserDetailsService.InMemoryUserDetailsManager bypasses the database.To use DB users, I implemented a custom UserDetailsService backed by JPA
/*
Spring Security
   ↓
UserDetailsService (DB)
   ↓
UserRepository (JPA)
   ↓
MySQL users table

 */