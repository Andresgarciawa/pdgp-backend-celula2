package com.celula2.auth.authservice.service.implementation;

import com.celula2.auth.authservice.aplication.DTO.LoginRequest;
import com.celula2.auth.authservice.aplication.DTO.TokenResponse;
import com.celula2.auth.authservice.aplication.exceptions.UnauthorizedException;
import com.celula2.auth.authservice.aplication.repositories.UserRepository;
import com.celula2.auth.authservice.service.AuthService;
import com.celula2.auth.authservice.service.JwtTokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImp implements AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwt;

    public AuthServiceImp(UserRepository users, PasswordEncoder passwordEncoder, JwtTokenService jwt) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    public TokenResponse login(LoginRequest req) {
        var user = users.findByUsername(req.username())
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS"));

        if (!user.isEnabled() || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("INVALID_CREDENTIALS");
        }

        var roles = user.getRoles().stream().map(r -> r.getName()).toList();
        var token = jwt.createAccessToken(user.getId(), user.getUsername(), roles);
        return new TokenResponse(token, "Bearer", user.getUsername(), roles.getFirst(), jwt.accessTtlSeconds());
    }
}