package com.example.demo.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String accountName;
    private String nickname;
    private String password;
    private Integer genderId;
    private Integer genderMatch;
    private String image;

    public User() {
        // no-argument constructor
    }

    public User(Integer id) {
        this.id = id;
    }

}

