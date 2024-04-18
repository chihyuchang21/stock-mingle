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

    //    @ManyToOne
//    @JoinColumn(name = "gender", referencedColumnName = "id")
    private Integer genderId;

    //    @ManyToOne
//    @JoinColumn(name = "gender_match", referencedColumnName = "id")
    private Integer genderMatch;

    private String image;

    public User() {
        // no-argument constructor
    }

//    public User(Integer id, String accountName, String nickname, Integer genderId) {
//        this.id = id;
//        this.accountName = accountName;
//        this.nickname = nickname;
//        this.genderId = genderId;
//    }
}

