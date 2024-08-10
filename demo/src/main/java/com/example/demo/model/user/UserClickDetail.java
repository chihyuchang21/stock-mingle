package com.example.demo.model.user;

import com.example.demo.model.article.Category;
import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "favorite_topic", referencedColumnName = "id")
    private Category favoriteTopic;

    // Resolve the issue of duplicate column mapping raised by Hibernate
    @ManyToOne
    @JoinColumn(name = "recommend_topic1", referencedColumnName = "id")
    private Category recommendTopic1;

    @ManyToOne
    @JoinColumn(name = "recommend_topic2", referencedColumnName = "id")
    private Category recommendTopic2;
}
