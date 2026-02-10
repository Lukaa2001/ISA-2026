package com.jutjubic.backend.controller;

import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.service.UserService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{id}")
  public Map<String, Object> getProfile(@PathVariable long id) {
    return userService.getProfile(id);
  }

  @GetMapping("/{id}/videos")
  public Map<String, Object> getUserVideos(
      @PathVariable long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "12") int limit
  ) {
    if (page <= 0 || limit <= 0 || limit > 50) {
      throw new ApiException(400, "Invalid pagination parameters");
    }

    return userService.getUserVideos(id, page, limit);
  }
}
