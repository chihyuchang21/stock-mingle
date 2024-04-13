package com.example.demo.model.article;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;

    @ManyToOne
    @JoinColumn(name = "categoryId") //FK
    private Category categoryId;

    private String content;
    private Integer userId;
    private Integer likeCount;
    private Integer commentCount;

    // 定義setCategory 方法
    public void setCategory(Category category) {
        this.categoryId = category;
    }

}
