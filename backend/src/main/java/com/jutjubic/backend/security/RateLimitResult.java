package com.jutjubic.backend.security;

public record RateLimitResult(boolean allowed, int currentCount, int limit, long retryAfterMs) {}
