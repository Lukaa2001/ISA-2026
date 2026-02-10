package com.jutjubic.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
  private final StringRedisTemplate stringRedisTemplate;
  private final RedisTemplate<String, byte[]> byteArrayRedisTemplate;
  private final ObjectMapper objectMapper;

  public CacheService(
      StringRedisTemplate stringRedisTemplate,
      RedisTemplate<String, byte[]> byteArrayRedisTemplate,
      ObjectMapper objectMapper
  ) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.byteArrayRedisTemplate = byteArrayRedisTemplate;
    this.objectMapper = objectMapper;
  }

  public <T> T get(String key, TypeReference<T> typeReference) {
    String raw = stringRedisTemplate.opsForValue().get(key);
    if (raw == null) {
      return null;
    }

    try {
      return objectMapper.readValue(raw, typeReference);
    } catch (Exception ex) {
      return null;
    }
  }

  public void set(String key, Object value, long ttlSeconds) {
    try {
      String raw = objectMapper.writeValueAsString(value);
      stringRedisTemplate.opsForValue().set(key, raw, Duration.ofSeconds(ttlSeconds));
    } catch (Exception ignored) {
      // Cache failures should not fail request handling.
    }
  }

  public byte[] getBuffer(String key) {
    return byteArrayRedisTemplate.opsForValue().get(key);
  }

  public void setBuffer(String key, byte[] data, long ttlSeconds) {
    byteArrayRedisTemplate.opsForValue().set(key, data, Duration.ofSeconds(ttlSeconds));
  }

  public void delPattern(String pattern) {
    Set<String> keys = stringRedisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      stringRedisTemplate.delete(keys);
    }
  }
}
