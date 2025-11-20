package com.akash.auth.entity;


import com.akash.auth.entity.Enum.Provider;
import com.akash.auth.entity.Enum.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String password; // null for OAuth-only users
    private String image;

    //which time create or update
    private boolean enabled = true;
    private Instant createdAt = Instant.now();  //2025-11-20T10:51:30.123456Z
    private Instant updatedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private Provider provider=Provider.LOCAL; // if we don't use any provider that time used Local(username,pw)

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}