package com.example.demo.service;

import com.example.demo.model.message.Message;
import com.example.demo.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public Message saveMessage(Message message) {
        return chatRoomRepository.save(message);
    }

    public List<Message> retrieveMessage(Integer userPairingHistoryId) {
        return chatRoomRepository.findByUserPairingHistoryId(userPairingHistoryId);
    }
}
