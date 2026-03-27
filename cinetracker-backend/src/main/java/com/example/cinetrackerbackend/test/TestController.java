package com.example.cinetrackerbackend.test;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController{

  private final UserRepository userRepository;
  
  @GetMapping("/test-db")
  public String testDb(){

    return "User saved!";
  }
  
  @GetMapping("/error")
  public String testError(){
    return "Error Screen";
  }

}
