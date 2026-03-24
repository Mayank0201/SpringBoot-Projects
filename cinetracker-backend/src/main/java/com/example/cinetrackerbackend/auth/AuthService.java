package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService{

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

}
