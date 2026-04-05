package com.example.cinetrackerbackend.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest{
    @NotBlank(message="Username is required")
    private String username;

    @NotBlank(message="Password is required")
    private String password;
}
//these dtos are put into place to avoid exposing the entire user entity to the client, which may contain sensitive information such as hashed passwords. By using these request objects, we can control exactly what data is sent from the client to the server during authentication processes.
//or the user exploiting this and can put any roles for themselves 