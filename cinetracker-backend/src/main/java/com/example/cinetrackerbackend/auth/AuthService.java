package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthService{

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User register(String username,String email,String password){
    
    if (userRepository.existsByUsername(username)){
      throw new RuntimeException("Username already exists");
    }

    if(userRepository.existsByEmail(email)){
      throw new RuntimeException("Email address already in use");
    }
    
    User user=new User(username,email,passwordEncoder.encode(password));
    return userRepository.save(user);
      
  }

  public User login(String username,String password){
    User user = userRepository.findByUsername(username).orElseThrow(
      () -> new RuntimeException("User not found"));

    if(!passwordEncoder.matches(password, user.getPassword())){
      throw new RuntimeException("Invalid Password");
    }
    return user;

  }

}
