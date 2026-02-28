package com.celula2.auth.authservice.service;

import java.util.List;

public interface JwtTokenService {
    String createAccessToken(Long userId, String username, List<String> roles);

    boolean isValid(String token);

    Long getUserId(String token);

    String getUsername(String token);

    @SuppressWarnings("unchecked")
    List<String> getRoles(String token);

    public long accessTtlSeconds();

}
