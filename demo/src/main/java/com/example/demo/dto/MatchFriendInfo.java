package com.example.demo.dto;

import lombok.Data;


@Data
public class MatchFriendInfo {
    private String nickname;
    private String image;

    public MatchFriendInfo(String nickname, String image) {
        this.nickname = nickname;
        this.image = image;
    }

}

