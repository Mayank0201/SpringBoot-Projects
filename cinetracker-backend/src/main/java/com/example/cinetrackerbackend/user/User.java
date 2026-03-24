package com.example.cinetrackerbackend.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
@Data
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column(nullable=false,unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable=false,unique=false)
    private String password;

    public User(String username, String email, String password) {
        this.username=username;
        this.email=email;
        this.password=password;
    }
}
