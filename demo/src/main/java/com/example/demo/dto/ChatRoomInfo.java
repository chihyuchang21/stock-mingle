package com.example.demo.dto;

import lombok.Data;

@Data
public class ChatRoomInfo {
    private Integer chatroomId;
    private Integer otherUserId;
    private String otherUserNickname;
    private String otherUserImage;

    public ChatRoomInfo() {
    }

    public ChatRoomInfo(Integer chatroomId, Integer otherUserId, String otherUserNickname, String otherUserImage) {
        this.chatroomId = chatroomId;
        this.otherUserId = otherUserId;
        this.otherUserNickname = otherUserNickname;
        this.otherUserImage = otherUserImage;
    }
}
