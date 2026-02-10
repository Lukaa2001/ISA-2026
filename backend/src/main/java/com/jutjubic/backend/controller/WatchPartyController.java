package com.jutjubic.backend.controller;

import com.jutjubic.backend.security.JwtPrincipal;
import com.jutjubic.backend.security.SecurityUtils;
import com.jutjubic.backend.service.WatchPartyService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watch-party")
public class WatchPartyController {
  private final WatchPartyService watchPartyService;

  public WatchPartyController(WatchPartyService watchPartyService) {
    this.watchPartyService = watchPartyService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create() {
    JwtPrincipal principal = SecurityUtils.getCurrentPrincipal();
    return watchPartyService.create(principal.userId());
  }

  @GetMapping("/{roomCode}")
  public Map<String, Object> getRoom(@PathVariable String roomCode) {
    return watchPartyService.getRoom(roomCode);
  }

  @PostMapping("/{roomCode}/join")
  public Map<String, Object> join(@PathVariable String roomCode) {
    JwtPrincipal principal = SecurityUtils.getCurrentPrincipal();
    return watchPartyService.join(roomCode, principal.userId());
  }

  @DeleteMapping("/{roomCode}")
  public Map<String, Object> close(@PathVariable String roomCode) {
    JwtPrincipal principal = SecurityUtils.getCurrentPrincipal();
    return watchPartyService.close(roomCode, principal.userId());
  }
}
