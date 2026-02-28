package com.celula2.auth.authservice.service;

import com.celula2.auth.authservice.aplication.DTO.LoginRequest;
import com.celula2.auth.authservice.aplication.DTO.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest req);
}
