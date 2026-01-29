package com.akash.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Spring Boot Auth API",
                description = "Spring Boot Auth API Documentation",
                contact = @Contact(
                        name = "Akash",
                        url = "https://akashjena9208.github.io/Akash_Personal_Portfolio/",
                        email = "akashejan9208@gmail.com"
                ),
                version = "1.0",
                summary = "This app is very useful if you don’t want to create an auth app from scratch."
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer", // Authorization: Bearer <token>
        bearerFormat = "JWT"
)
public class APIDocConfig {
}