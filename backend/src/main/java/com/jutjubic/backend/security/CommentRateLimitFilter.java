package com.jutjubic.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CommentRateLimitFilter extends OncePerRequestFilter {
  private static final long WINDOW_MS = 60L * 60 * 1000;
  private static final int MAX_HITS = 60;
  private static final Pattern PATH_PATTERN = Pattern.compile("^/api/videos/\\d+/comments$");

  private final SlidingWindowRateLimiterService rateLimiter;
  private final ObjectMapper objectMapper;

  public CommentRateLimitFilter(SlidingWindowRateLimiterService rateLimiter, ObjectMapper objectMapper) {
    this.rateLimiter = rateLimiter;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equalsIgnoreCase(request.getMethod())
        || !PATH_PATTERN.matcher(request.getServletPath()).matches();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
      filterChain.doFilter(request, response);
      return;
    }

    String key = "rl:comment:" + principal.userId();
    RateLimitResult result = rateLimiter.checkSlidingWindow(key, WINDOW_MS, MAX_HITS);

    setRateLimitHeaders(response, result);

    if (!result.allowed()) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("Retry-After", String.valueOf((int) Math.ceil(result.retryAfterMs() / 1000.0)));
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("error", "Too many requests");
      body.put("retryAfterMs", result.retryAfterMs());
      objectMapper.writeValue(response.getWriter(), body);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void setRateLimitHeaders(HttpServletResponse response, RateLimitResult result) {
    response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, result.limit() - result.currentCount())));
  }
}
