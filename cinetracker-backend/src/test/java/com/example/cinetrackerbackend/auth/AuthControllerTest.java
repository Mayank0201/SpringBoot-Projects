package com.example.cinetrackerbackend.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @Test
    void testRegisterRequestCanBeCreatedWithConstructor() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "TestPassword123!";
        
        RegisterRequest request = new RegisterRequest(username, email, password);
        
        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testLoginRequestCanBeCreatedWithConstructor() {
        String username = "testuser";
        String password = "TestPassword123!";
        
        LoginRequest request = new LoginRequest(username, password);
        
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testRefreshTokenRequestCanBeCreatedWithConstructor() {
        String refreshToken = "valid.refresh.token";
        
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        assertEquals(refreshToken, request.getRefreshToken());
    }

    @Test
    void testRegisterRequestCanBeCreatedWithNoArgsConstructor() {
        RegisterRequest request = new RegisterRequest();
        
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("TestPassword123!");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("TestPassword123!", request.getPassword());
    }

    @Test
    void testLoginRequestCanBeCreatedWithNoArgsConstructor() {
        LoginRequest request = new LoginRequest();
        
        request.setUsername("testuser");
        request.setPassword("TestPassword123!");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("TestPassword123!", request.getPassword());
    }

    @Test
    void testRefreshTokenRequestCanBeCreatedWithNoArgsConstructor() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        
        request.setRefreshToken("valid.refresh.token");
        
        assertEquals("valid.refresh.token", request.getRefreshToken());
    }
}
