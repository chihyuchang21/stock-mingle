package com.example.demo.model.user;

import com.example.demo.model.article.Category;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class UserClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId; //待改為FK

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category categoryId;

    private Timestamp timestamp;
}
