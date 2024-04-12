package com.example.demo.model.user;

import lombok.Data;

//存放計算結果
@Data
public class UserSimilarity {
    private Integer userId1;
    private Integer userId2;
    private Double similarity;

    public UserSimilarity(Integer userId1, Integer userId2, Double similarity) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.similarity = similarity;
    }

    public UserSimilarity(Integer userId1, Integer mostSimilarUserId) {
        this.userId1 = userId1;
        this.userId2 = mostSimilarUserId;
    }
}

