package com.jutjubic.backend.security;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SlidingWindowRateLimiterService {
  private final StringRedisTemplate redisTemplate;

  public SlidingWindowRateLimiterService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public RateLimitResult checkSlidingWindow(String key, long windowMs, int maxHits) {
    long now = System.currentTimeMillis();
    long currentWindow = now / windowMs;
    long previousWindow = currentWindow - 1;
    long elapsedInWindow = now - currentWindow * windowMs;
    double weight = 1.0 - (double) elapsedInWindow / windowMs;

    String prevKey = key + ":" + previousWindow;
    String currKey = key + ":" + currentWindow;

    int prevCount = parseInt(redisTemplate.opsForValue().get(prevKey));
    Long currCountLong = redisTemplate.opsForValue().increment(currKey);
    int currCount = currCountLong == null ? 0 : currCountLong.intValue();
    redisTemplate.expire(currKey, Duration.ofMillis(windowMs * 2));

    int estimatedCount = (int) Math.floor(prevCount * weight) + currCount;

    if (estimatedCount > maxHits) {
      redisTemplate.opsForValue().decrement(currKey);
      long retryAfterMs = windowMs - elapsedInWindow;
      return new RateLimitResult(false, estimatedCount, maxHits, retryAfterMs);
    }

    return new RateLimitResult(true, estimatedCount, maxHits, 0);
  }

  private int parseInt(String value) {
    if (value == null || value.isBlank()) {
      return 0;
    }

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return 0;
    }
  }
}
