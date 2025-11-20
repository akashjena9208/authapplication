package com.akash.auth.dto;

public record UserCreateDTO(
        String email,
        String name,
        String password
) {}
