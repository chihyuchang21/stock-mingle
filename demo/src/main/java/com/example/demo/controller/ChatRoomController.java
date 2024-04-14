package com.example.demo.controller;

import com.example.demo.model.message.Message;
import com.example.demo.service.ChatRoomService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;


    // 不同聊天室的訂閱路徑
    private static final String GREETINGS_TOPIC_PREFIX = "/topic/chats/";

//    @MessageMapping("/hello")
//    @SendTo("/topic/greetings") //
//    public Message sendMessage(Message message) {
//        Message savedMessage = chatRoomService.saveMessage(message);
//        return savedMessage;
//    }

    // 訂閱路徑動態生成
    @MessageMapping("/hello/{userPairingHistoryId}")
    @SendTo(GREETINGS_TOPIC_PREFIX + "{userPairingHistoryId}") // 動態生成路徑
    public Message sendMessageToSpecificRoom(@DestinationVariable String userPairingHistoryId, Message message) {
        Message savedMessage = chatRoomService.saveMessage(message);
        return savedMessage;
    }
}


