package com.example.cinetrackerbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.security.JwtFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@RequiredArgsConstructor
public class SecurityConfig{

  private final RateLimitFilter rateLimitFilter;
  private final JwtFilter jwtFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

    http
	  .csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\",\"status\":401,\"timestamp\":null,\"data\":null}");
    }))
	  .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**","/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info")
    .permitAll().anyRequest().authenticated()
    ).addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(jwtFilter, RateLimitFilter.class);
    
    return http.build();

  }

}
