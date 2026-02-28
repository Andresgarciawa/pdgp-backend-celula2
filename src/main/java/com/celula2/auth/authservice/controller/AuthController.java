package com.celula2.auth.authservice.controller;


import com.celula2.auth.authservice.AuthServiceApplication;
import com.celula2.auth.authservice.aplication.DTO.LoginRequest;
import com.celula2.auth.authservice.aplication.DTO.TokenResponse;
import com.celula2.auth.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/testAutenticacion")
    public String TestUrl() {
        return "Authenticated!";
    }
}