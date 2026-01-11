package com.akash.auth.entity;


import com.akash.auth.entity.enums.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 100)
    private String name;
    private String password; // null for OAuth-only users
    private String image;

    //which time create or update
    private boolean enabled = true;
    private Instant createdAt = Instant.now();  //we used  Instant bcz  UTC format  represent  example :-2025-11-20T10:51:30.123456Z
    private Instant updatedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private Provider provider=Provider.LOCAL; // if we don't use any provider that time used Local(username,pw)

    @ManyToMany(fetch = FetchType.EAGER) //when we fetch the user that time in db roles also come    default is lazy so that reason we do egar
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    //@PrePersist and @PreUpdate They are JPA lifecycle callback annotations. They allow us to run code automatically before data is INSERTED into the database before data is UPDATED in the database
    //@PrePersist why used bcz Before a new record is inserted into the database.To set values automatically, Especially for: createdAt updatedAt
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
// @PreUpdate – WHY we use it Before an existing record is updated. To update updatedAt every time data changes No need to manually write update logic
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }





    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}