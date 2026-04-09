package com.example.demo.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SignupRequest (
        @NotBlank
        String username,

        @Size(max = 50)
        @NotBlank
        @Email
        String email,

        @Size(min = 6, max = 40)
        @NotBlank
        String password,
        Set<String> role

){ }
