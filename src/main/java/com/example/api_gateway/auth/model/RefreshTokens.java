package com.example.api_gateway.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokens {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime issued_at;

    @Column(nullable = false)
    private LocalDateTime expires_at;

    @Column(nullable = false)
    private boolean revoked;

    @Column(columnDefinition = "TEXT")
    private String clientInfo;

}
