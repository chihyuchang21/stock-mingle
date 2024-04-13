package com.example.demo.model.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "hashtag_name")
    private String hashtagName;
}
