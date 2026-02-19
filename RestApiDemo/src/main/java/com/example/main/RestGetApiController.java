package com.example.main;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
//works with Controller
//we use restcontroller if we want to use rest operations
//like we can put the responseEntity type as a class and then we can get a
//json response for the class properties for the same if we pass it as a body inside the ResponseEntity
//like below using Person
//if i tried same without response entity , controller will not work

//your code works because you're returning ResponseEntity<...>, which tells Spring to serialize the object, even though you’re using @Controller.
//
//but if you're only building APIs, switch to @RestController, so you don’t have to worry about @ResponseBody or accidental view resolution.

//@RestController = @Controller + @ResponseBody
//
//use @RestController for APIs.
//
//use @Controller for web pages (frontend rendering).

public class RestGetApiController {

    @GetMapping("/hello/{name}")
    public String greet(@PathVariable String name){
        return "Hello "+name+"!";
    }
    //self explanatory for the path variable

    @GetMapping("/hello")
    public String greet2(@RequestParam(defaultValue = "User") String greet){
        return "Hello "+greet+"!";
    }
    //http://localhost:8080/hello?greet=mayank will return hello mayank , else hello user


    @GetMapping("/status")
    public ResponseEntity<String> customResponse(){
        //inspecting will get us the status code in the website
        return new ResponseEntity<>("<h1>Accepted</h1>", HttpStatus.ACCEPTED);
        //CHECK HTTPSTATUS CLASS LATER
    }

    @GetMapping("/person")
    public ResponseEntity<Person> person(){
        Person person=new Person("John",10);
        return new ResponseEntity<>(person,HttpStatus.CREATED);
    }

}
