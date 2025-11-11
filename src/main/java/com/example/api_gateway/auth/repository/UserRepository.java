package com.example.api_gateway.auth.repository;

import com.example.api_gateway.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existByUsername(String username);
    Optional<User> findByEmail(String email);
}
