package com.jutjubic.backend.repository;

import com.jutjubic.backend.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByActivationToken(String activationToken);

  Optional<User> findByEmailOrUsername(String email, String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
