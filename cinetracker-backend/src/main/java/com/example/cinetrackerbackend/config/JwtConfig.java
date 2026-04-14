package com.example.cinetrackerbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {

    private String secret; //since prefix jwt used , it will map to jwt.secret from app.prop
    private long expiration;
    private long refreshExpiration;

}
