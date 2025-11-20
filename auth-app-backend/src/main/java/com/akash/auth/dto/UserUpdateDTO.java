package com.akash.auth.dto;

public record UserUpdateDTO(
        String name,
        String image,
        boolean enabled
) {}
