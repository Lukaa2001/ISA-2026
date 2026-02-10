package com.jutjubic.backend.config;

import com.jutjubic.backend.security.ApiAuthenticationEntryPoint;
import com.jutjubic.backend.security.CommentRateLimitFilter;
import com.jutjubic.backend.security.JwtAuthenticationFilter;
import com.jutjubic.backend.security.LoginRateLimitFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      ApiAuthenticationEntryPoint authenticationEntryPoint,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      LoginRateLimitFilter loginRateLimitFilter,
      CommentRateLimitFilter commentRateLimitFilter
  ) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/activate/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/videos/**", "/api/users/**").permitAll()
            .requestMatchers("/uploads/**", "/api-docs", "/api-docs/**", "/api-docs.json", "/api-docs.json/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(commentRateLimitFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(
        frontendUrl,
        "http://localhost:5173",
        "http://localhost:5174",
        "http://*.ngrok.io",
        "https://*.ngrok.io",
        "http://*.ngrok-free.dev",
        "https://*.ngrok-free.dev",
        "*"
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("X-RateLimit-Limit", "X-RateLimit-Remaining", "Retry-After"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
