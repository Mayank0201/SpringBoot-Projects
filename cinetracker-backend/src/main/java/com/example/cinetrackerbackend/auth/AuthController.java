package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController{
    private final AuthService authService;

    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest request){
        return authService.register(request.username,
            request.email,
            request.password);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){

        return authService.login(request.username, request.password);

    }
}