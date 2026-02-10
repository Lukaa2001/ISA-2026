package com.jutjubic.backend.security;

import com.jutjubic.backend.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static JwtPrincipal getCurrentPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
      throw new ApiException(401, "Authentication required");
    }

    return principal;
  }
}
