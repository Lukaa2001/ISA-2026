package com.jutjubic.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey signingKey;
  private final long expiresInMs;

  public JwtService(
      @Value("${app.jwt.secret:dev-secret-key}") String secret,
      @Value("${app.jwt.expires-in:24h}") String expiresIn
  ) {
    this.signingKey = Keys.hmacShaKeyFor(normalizeSecret(secret));
    this.expiresInMs = parseDurationMs(expiresIn);
  }

  public String generateToken(Long userId, String email) {
    Instant now = Instant.now();
    return Jwts.builder()
        .claim("userId", userId)
        .claim("email", email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expiresInMs)))
        .signWith(signingKey)
        .compact();
  }

  public JwtPrincipal verifyToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    Number userId = claims.get("userId", Number.class);
    String email = claims.get("email", String.class);
    if (userId == null || email == null) {
      throw new IllegalArgumentException("Invalid token payload");
    }

    return new JwtPrincipal(userId.longValue(), email);
  }

  private byte[] normalizeSecret(String secret) {
    byte[] base = secret.getBytes(StandardCharsets.UTF_8);
    if (base.length >= 32) {
      return base;
    }

    byte[] expanded = new byte[32];
    for (int i = 0; i < expanded.length; i++) {
      expanded[i] = base[i % base.length];
    }
    return expanded;
  }

  private long parseDurationMs(String value) {
    String trimmed = value.trim().toLowerCase();
    if (trimmed.endsWith("ms")) {
      return Long.parseLong(trimmed.substring(0, trimmed.length() - 2));
    }

    char unit = trimmed.charAt(trimmed.length() - 1);
    long number;
    if (Character.isDigit(unit)) {
      return Long.parseLong(trimmed);
    }

    number = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
    return switch (unit) {
      case 's' -> number * 1000;
      case 'm' -> number * 60 * 1000;
      case 'h' -> number * 60 * 60 * 1000;
      case 'd' -> number * 24 * 60 * 60 * 1000;
      default -> throw new IllegalArgumentException("Unsupported JWT expiration format: " + value);
    };
  }
}
