package com.example.cinetrackerbackend.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailResponse {
    private Long userId;
    private String username;
    private String email;
    private String message;

    public static VerifyEmailResponse of(Long userId, String username, String email) {
        VerifyEmailResponse response = new VerifyEmailResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setEmail(email);
        response.setMessage("Email verified successfully");
        return response;
    }
}
