package com.jutjubic.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViewCounterService {
  private final JdbcTemplate jdbcTemplate;

  public ViewCounterService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public int increment(long videoId) {
    Integer result = jdbcTemplate.queryForObject(
        "UPDATE videos SET view_count = view_count + 1 WHERE id = ? RETURNING view_count",
        Integer.class,
        videoId
    );

    return result == null ? 0 : result;
  }
}
