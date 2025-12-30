package com.akash.auth.dto;

import com.akash.auth.dto.enums.RoleDTO;
import com.akash.auth.entity.enums.Provider;
import lombok.Builder;
import lombok.Setter;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enable = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Provider provider = Provider.LOCAL;
    private Set<RoleDTO> roles = new HashSet<>();
}
///I used a class instead of a record because my UserDto is mutable and reused across multiple flows like registration, login, and admin operations. It contains sensitive fields like password, default values, and fields that need to be modified before sending a response. Since record is immutable and intended for response-only DTOs, a class is the correct choice here.