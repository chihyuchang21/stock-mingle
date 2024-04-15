package com.example.demo.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserClickDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer companyNewsClick;
    private Integer broadMarketNewsClick;
    private Integer companyDiscussionClick;
    private Integer adviceRequestClick;
    private Integer othersClick;
    private String favoriteTopic;
    private String recommendTopic1;
    private String recommendTopic2;

}
