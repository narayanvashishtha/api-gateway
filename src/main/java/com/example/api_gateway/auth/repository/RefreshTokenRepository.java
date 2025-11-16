package com.example.api_gateway.auth.repository;

import com.example.api_gateway.auth.model.RefreshTokens;
import com.example.api_gateway.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, UUID> {
    void deleteByUser(User user);
    List<RefreshTokens> findAllByUser(User user);
}
