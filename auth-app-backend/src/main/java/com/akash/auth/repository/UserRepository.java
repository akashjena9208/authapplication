package com.akash.auth.repository;

import com.akash.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    //“Optional helps avoid null handling and enforces explicit presence checks, making the code safer.”
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}