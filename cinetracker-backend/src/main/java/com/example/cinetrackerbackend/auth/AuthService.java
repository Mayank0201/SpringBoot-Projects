package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService{

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public User register(String username,String email,String password){

    if (userRepository.existsByUsername(username)){
      throw new ApiException("Username already exists", HttpStatus.CONFLICT);
    }

    if(userRepository.existsByEmail(email)){
      throw new ApiException("Email address already in use", HttpStatus.CONFLICT);
    }

    User user = new User(username, email, passwordEncoder.encode(password));
    User savedUser = userRepository.save(user);
    return savedUser;
  }

  public String login(String username,String password){

    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED));

    if(!passwordEncoder.matches(password, user.getPassword())){
      throw new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    String token = jwtService.generateToken(user);
    return token;
  }

}
