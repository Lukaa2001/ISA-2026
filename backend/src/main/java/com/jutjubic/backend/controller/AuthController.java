package com.jutjubic.backend.controller;

import com.jutjubic.backend.dto.LoginRequest;
import com.jutjubic.backend.dto.RegisterRequest;
import com.jutjubic.backend.security.JwtPrincipal;
import com.jutjubic.backend.security.SecurityUtils;
import com.jutjubic.backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @GetMapping("/activate/{token}")
  public Map<String, Object> activate(@PathVariable String token) {
    return authService.activate(token);
  }

  @PostMapping("/login")
  public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @GetMapping("/me")
  public Map<String, Object> me() {
    JwtPrincipal principal = SecurityUtils.getCurrentPrincipal();
    return authService.getMe(principal.userId());
  }
}
