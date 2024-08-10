package com.example.demo.model.article;

import com.example.demo.model.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ArticleComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String content;
    private String articleId;

    @ManyToOne
    @JoinColumn(name = "userId") // FK
    private User userId;
}
