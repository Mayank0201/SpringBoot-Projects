package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController{
    private final AuthService authService;

    @PostMapping({"/register", "/register/"})
    public ResponseEntity<AuthUserResponse> register(@Valid @RequestBody RegisterRequest request){
        User createdUser = authService.register(request.getUsername(),
            request.getEmail(),
            request.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthUserResponse(createdUser.getId(), createdUser.getUsername(), createdUser.getEmail()));
    }

    @PostMapping({"/login", "/login/"})
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping({"/refresh", "/refresh/", "/refresh-token", "/refresh-token/"})
    public RefreshTokenResponse refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request.getRefreshToken());
    }

}