package com.example.demo.model.article;

import com.example.demo.model.user.User;
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
    @JoinColumn(name = "categoryId") // FK
    private Category categoryId;

    private String content;

    @ManyToOne
    @JoinColumn(name = "userId") // FK
    private User userId;

    private Integer likeCount;
    private Integer commentCount;

    @Transient // DB does not have this column
    private String nickname;

    private String image;

    // Define setCategory method
    public void setCategory(Category category) {
        this.categoryId = category;
    }

    public String getUserNickname() {
        if (this.userId != null) {
            return this.userId.getNickname();
        } else {
            return null;
        }
    }
}
