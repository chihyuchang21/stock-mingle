package com.example.demo.dto;

import lombok.Data;


@Data
public class MatchFriendInfo {
    private Integer pairingHistoryId;
    private String nickname;
    private String image;

    public MatchFriendInfo(String nickname, String image, Integer pairingHistoryId) {
        this.nickname = nickname;
        this.image = image;
        this.pairingHistoryId = pairingHistoryId;
    }

}

