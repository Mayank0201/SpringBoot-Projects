package com.example.cinetrackerbackend.exception;

import com.example.cinetrackerbackend.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Object>> handleAPiException(ApiException ex){

    return new ResponseEntity<>(
      ApiResponse.error(ex.getMessage(), ex.getStatus().value(), null),
      ex.getStatus()
    );

  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );

    return new ResponseEntity<>(
      ApiResponse.error("Validation failed", HttpStatus.BAD_REQUEST.value(), errors),
      HttpStatus.BAD_REQUEST
    );
}

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
    return new ResponseEntity<>(
      ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null),
      HttpStatus.INTERNAL_SERVER_ERROR
    );
  }

}
