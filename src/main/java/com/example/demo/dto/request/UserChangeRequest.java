package com.example.demo.dto.request;

public record UserChangeRequest (
        String username,
        String email,
        String imageUrl,
        String currentPassword,
        String newPassword
){
}
