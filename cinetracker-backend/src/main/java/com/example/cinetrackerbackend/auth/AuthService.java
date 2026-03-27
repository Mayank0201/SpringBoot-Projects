package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;

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
      throw new RuntimeException("Username already exists");
    }

    if(userRepository.existsByEmail(email)){
      throw new RuntimeException("Email address already in use");
    }

    User user = new User(username, email, passwordEncoder.encode(password));
    User savedUser = userRepository.save(user);
    return savedUser;
  }

  public String login(String username,String password){

    User user = userRepository.findByUsername(username).orElseThrow(() -> {
      return new RuntimeException("User not found");
    });

    if(!passwordEncoder.matches(password, user.getPassword())){
      throw new RuntimeException("Invalid Password");
    }

    String token = jwtService.generateTokens(user.getUsername());
    return token;
  }

}
