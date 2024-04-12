package com.example.demo.service;

import com.example.demo.model.message.Message;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public Message saveMessage(Message message){
        return chatRoomRepository.save(message);
    }
}
