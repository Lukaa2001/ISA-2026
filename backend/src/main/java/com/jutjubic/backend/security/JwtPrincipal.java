package com.jutjubic.backend.security;

public record JwtPrincipal(Long userId, String email) {}
