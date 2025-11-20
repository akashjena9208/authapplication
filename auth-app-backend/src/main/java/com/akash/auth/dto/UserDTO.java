package com.akash.auth.dto;

import com.akash.auth.dto.Enum.RoleDTO;
import com.akash.auth.entity.Enum.Provider;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String email,
        String name,
        String image,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        Provider provider,
        Set<RoleDTO> roles
) {}
