package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<AuthUserResponse>> register(@Valid @RequestBody RegisterRequest request){
        User createdUser = authService.register(request.getUsername(),
            request.getEmail(),
            request.getPassword());

        AuthUserResponse response = new AuthUserResponse(createdUser.getId(), createdUser.getUsername(), createdUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully. Please check your email to verify your account.", HttpStatus.CREATED.value(), response));
    }

    @PostMapping({"/login", "/login/"})
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(@Valid @RequestBody LoginRequest request){
        AuthTokenResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Login successful", HttpStatus.OK.value(), response));
    }

    @PostMapping({"/refresh", "/refresh/", "/refresh-token", "/refresh-token/"})
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Access token refreshed", HttpStatus.OK.value(), response));
    }

    @PostMapping({"/verify-email", "/verify-email/"})
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmailResponse response = authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", HttpStatus.OK.value(), response));
    }

    @PostMapping({"/resend-verification", "/resend-verification/"})
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Verification email resent successfully", HttpStatus.OK.value(), "Check your email for the verification link"));
    }

}