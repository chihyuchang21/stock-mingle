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
    private Long id;

    private String accountName;
    private String nickname;
    private Integer genderId;
    private Integer genderMatch;
    private String favoriteTopic;
    private String image;

    public User() {
        // no-argument constructor
    }

    public User(Long id, String accountName, String nickname, Integer genderId) {
        this.id = id;
        this.accountName = accountName;
        this.nickname = nickname;
        this.genderId = genderId;
    }


}

