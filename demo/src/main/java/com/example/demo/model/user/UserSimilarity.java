package com.example.demo.model.user;

import lombok.Data;

@Data
public class UserSimilarity {
    private Long userId1;
    private Long userId2;
    private Double similarity;

    public UserSimilarity(Long userId1, Long userId2, Double similarity) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.similarity = similarity;
    }
}

