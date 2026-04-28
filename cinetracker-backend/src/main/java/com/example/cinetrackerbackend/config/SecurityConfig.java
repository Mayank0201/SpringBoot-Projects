package com.example.cinetrackerbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.cinetrackerbackend.security.JwtFilter;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig{

  private final RateLimitFilter rateLimitFilter;
  private final JwtFilter jwtFilter;
  private final CorsFilter corsFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

    http
      .cors(Customizer.withDefaults())
	  .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .headers(headers -> headers
          .frameOptions(frame -> frame.deny())
          .xssProtection(xss -> xss.disable()) // Modern browsers handle this via CSP
          .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none';"))
      )
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
        "/actuator/info",
        "/privacy.html")
    .permitAll().anyRequest().authenticated()
    )
    .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterAfter(jwtFilter, RateLimitFilter.class);
    
    return http.build();

  }

}
