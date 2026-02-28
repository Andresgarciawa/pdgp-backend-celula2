package com.celula2.auth.authservice.aplication.DTO;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank (message = "Usuario es requerido")
        String username,
        @NotBlank (message = "Contraseña es requerida")
        String password) {
}
