package com.example.cinetrackerbackend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class RegisterRequest{
    
    @NotBlank(message="Username is required")
    public String username;
    
    @Email(message="Invalid email format")
    @NotBlank(message="Email is required")
    public String email;
    
    @Size(min=8,message="Password must be at least 8 characters long")
    @Pattern(
    regexp = "^(?=.*[A-Z])(?=.*\\d).*$",
    message = "Password must contain at least 1 uppercase letter and 1 number"
    )
    public String password;
}
