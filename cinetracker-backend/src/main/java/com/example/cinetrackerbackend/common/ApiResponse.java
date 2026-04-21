package com.example.cinetrackerbackend.common;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private int status;
    private Instant timestamp;
    private T data;

    public static <T> ApiResponse<T> success(String message, int status, T data) {
        return new ApiResponse<>(true, message, status, Instant.now(), data);
    }

    public static ApiResponse<Object> error(String message, int status, Object data) {
        return new ApiResponse<>(false, message, status, Instant.now(), data);
    }
}