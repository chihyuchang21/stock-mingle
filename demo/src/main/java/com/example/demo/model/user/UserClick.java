package com.example.demo.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer usClick;
    private Integer twClick;
    private Integer dtClick;
    private String favoriteTopic;
    private String recommendTopic1;
    private String recommendTopic2;
}
