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

  private final JwtFilter jwtFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

    http
	  .csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"message\":\"Unauthorized\",\"status\":401}");
    }))
	  .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**","/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html")
    .permitAll().anyRequest().authenticated()
    ).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
    
    return http.build();

  }

}
