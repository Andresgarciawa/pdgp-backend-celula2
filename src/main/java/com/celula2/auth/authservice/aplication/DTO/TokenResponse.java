package com.celula2.auth.authservice.aplication.DTO;

public record TokenResponse(
        String token,
        String type,
        String username,
        String role,
        Long expiresIn
) {}
