package com.akash.auth.mapper;

import com.akash.auth.dto.UserDto;
import com.akash.auth.dto.RoleDTO;
import com.akash.auth.entity.Role;
import com.akash.auth.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {
        // prevent object creation
    }

    // =========================
    // DTO → ENTITY
    // =========================
    public static User toEntity(UserDto dto) {

        if (dto == null) {
            return null;
        }

        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .name(dto.getName())
                .password(dto.getPassword())
                .image(dto.getImage())
                .enabled(dto.isEnable())
                .provider(dto.getProvider())
                .roles(mapRolesToEntity(dto.getRoles()))
                .build();
    }

    // =========================
    // ENTITY → DTO
    // =========================
    public static UserDto toDto(User entity) {

        if (entity == null) {
            return null;
        }

        return UserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                // ⚠️ password intentionally NOT mapped by default // .password(dto.getPassword())  //we not write this so when response time so null
                .image(entity.getImage())
                .enable(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .provider(entity.getProvider())
                .roles(mapRolesToDto(entity.getRoles()))
                .build();
    }

    // =========================
    // ROLE MAPPERS
    // =========================
    private static Set<Role> mapRolesToEntity(Set<RoleDTO> roleDTOs) {

        if (roleDTOs == null) {
            return Set.of();
        }

        return roleDTOs.stream()
                .map(roleDTO -> Role.builder()
                        .id(roleDTO.id())
                        .name(roleDTO.name())
                        .build())
                .collect(Collectors.toSet());
    }

    private static Set<RoleDTO> mapRolesToDto(Set<Role> roles) {

        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .map(role -> new RoleDTO(
                        role.getId(),
                        role.getName()
                ))
                .collect(Collectors.toSet());
    }
}
