package com.celula2.auth.authservice.service.implementation;

import com.celula2.auth.authservice.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenServiceImp implements JwtTokenService {

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;

    public JwtTokenServiceImp(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-ttl-seconds}") long accessTtlSeconds
    ) {
        // Importante: usa UTF-8 explícito para evitar diferencias por charset del sistema
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
    }

    @Override
    public String createAccessToken(Long userId, String username, List<String> roles) {
        var now = Instant.now();
        var exp = now.plusSeconds(accessTtlSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(
                        "uid", userId,
                        "roles", roles
                ))
                .signWith(key)
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Long getUserId(String token) {
        var claims = parseClaims(token);
        Object uid = claims.get("uid");

        if (uid == null) return null;

        if (uid instanceof Number n) return n.longValue();
        // Si en algún momento llega como String
        return Long.parseLong(uid.toString());
    }

    @Override
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        var claims = parseClaims(token);
        Object roles = claims.get("roles");

        if (roles == null) return List.of();

        // Normalmente jjwt lo devuelve como List<?> si lo guardaste como List<String>
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }

        // Fallback por si llega en otro formato
        return List.of(String.valueOf(roles));
    }

    @Override
    public long accessTtlSeconds() {
        return accessTtlSeconds;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}