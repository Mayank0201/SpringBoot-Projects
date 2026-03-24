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
    
    User user=new User("testUser2","test2@test.com","123");
    userRepository.save(user);
    return "User saved!";
  }
  
  @GetMapping("/error")
  public String testError(){
    return "Error Screen";
  }

}
