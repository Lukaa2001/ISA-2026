package com.jutjubic.backend.service;

import com.jutjubic.backend.dto.LoginRequest;
import com.jutjubic.backend.dto.RegisterRequest;
import com.jutjubic.backend.entity.User;
import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.repository.UserRepository;
import com.jutjubic.backend.security.JwtService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailService emailService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      EmailService emailService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.emailService = emailService;
  }

  @Transactional
  public Map<String, Object> register(RegisterRequest input) {
    User existing = userRepository.findByEmailOrUsername(input.getEmail(), input.getUsername()).orElse(null);
    if (existing != null) {
      if (existing.getEmail().equals(input.getEmail())) {
        throw new ApiException(409, "Email already registered");
      }
      throw new ApiException(409, "Username already taken");
    }

    User user = new User();
    user.setEmail(input.getEmail());
    user.setUsername(input.getUsername());
    user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
    user.setFirstName(input.getFirstName());
    user.setLastName(input.getLastName());
    user.setAddress(input.getAddress());
    user.setActivationToken(UUID.randomUUID().toString());

    user = userRepository.save(user);

    String previewUrl = emailService.sendActivationEmail(user.getEmail(), user.getActivationToken());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", user.getId());
    result.put("email", user.getEmail());
    result.put("username", user.getUsername());
    result.put("message", "Registration successful. Please check your email to activate your account.");
    result.put("emailPreviewUrl", previewUrl);
    return result;
  }

  @Transactional
  public Map<String, Object> activate(String token) {
    User user = userRepository.findByActivationToken(token)
        .orElseThrow(() -> new ApiException(400, "Invalid or already used activation token"));

    user.setActive(true);
    user.setActivationToken(null);
    userRepository.save(user);

    return Map.of("message", "Account activated successfully");
  }

  @Transactional(readOnly = true)
  public Map<String, Object> login(LoginRequest input) {
    User user = userRepository.findByEmail(input.getEmail())
        .orElseThrow(() -> new ApiException(401, "Invalid email or password"));

    if (!user.isActive()) {
      throw new ApiException(403, "Account not activated. Please check your email.");
    }

    if (!passwordEncoder.matches(input.getPassword(), user.getPasswordHash())) {
      throw new ApiException(401, "Invalid email or password");
    }

    String token = jwtService.generateToken(user.getId(), user.getEmail());

    Map<String, Object> userBody = new LinkedHashMap<>();
    userBody.put("id", user.getId());
    userBody.put("email", user.getEmail());
    userBody.put("username", user.getUsername());
    userBody.put("firstName", user.getFirstName());
    userBody.put("lastName", user.getLastName());

    return Map.of(
        "token", token,
        "user", userBody
    );
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getMe(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(404, "User not found"));

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", user.getId());
    body.put("email", user.getEmail());
    body.put("username", user.getUsername());
    body.put("firstName", user.getFirstName());
    body.put("lastName", user.getLastName());
    body.put("address", user.getAddress());
    body.put("createdAt", user.getCreatedAt());
    return body;
  }
}
