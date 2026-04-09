package com.example.demo.dto.response;


import java.util.List;

public record JwtResponse(
    Long id,
    String username,
    String email,
    String imageUrl,
    List<String> roles
) {
}
