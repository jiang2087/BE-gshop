package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    private String jti;

    private String username;


    @Column(nullable = false)
    private String tokenHash;


    private Instant createdAt;
    private Instant expiresAt;


    private boolean revoked = false;


    private String replacedBy; // jti of the previous token
}