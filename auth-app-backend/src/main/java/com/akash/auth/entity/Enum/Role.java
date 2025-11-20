package com.akash.auth.entity.Enum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Id
    private UUID id=UUID.randomUUID();
    @Column(unique = true, nullable = false)
    private String name; // ROLE_USER, ROLE_ADMIN

}