package com.example.cinetrackerbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
	  .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**","/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html")
    .permitAll().anyRequest().authenticated()
    ).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
    
    return http.build();

  }

}
