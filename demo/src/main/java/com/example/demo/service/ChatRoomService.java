package com.example.demo.service;

import com.example.demo.dto.ChatRoomInfo;
import com.example.demo.model.message.Message;
import com.example.demo.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public Message saveMessage(Message message) {
        return chatRoomRepository.save(message);
    }

    public List<Message> retrieveMessage(Integer userPairingHistoryId) {
        List<Message> messages = chatRoomRepository.findByUserPairingHistoryId(userPairingHistoryId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        messages.forEach(message -> {
            Timestamp timestamp = message.getSendTime();
            LocalDateTime localDateTime = timestamp.toLocalDateTime();
            String formattedTimestamp = localDateTime.format(formatter);
            message.setFormattedSendTime(formattedTimestamp);
        });


        return chatRoomRepository.findByUserPairingHistoryId(userPairingHistoryId);
    }

    public List<ChatRoomInfo> getChatroomInfoByUserId(Integer userId) {
        return chatRoomRepository.findChatroomInfoByUserId(userId);
    }

//    public ChatRoomInfo getCurrentChatroomByUserId(Integer userId, Integer currentChatroomId) {
//        return chatRoomRepository.findCurrentChatroomByUserId(userId, currentChatroomId);
//    }
}
