package com.example.demo.controller;

import com.example.demo.model.message.Message;
import com.example.demo.service.ChatRoomService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;


    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Message sendMessage(Message message) {
        Message savedMessage = chatRoomService.saveMessage(message);
        return savedMessage;
    }
}

