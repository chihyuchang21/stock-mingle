package com.example.demo.controller;

import com.example.demo.model.message.Message;
import com.example.demo.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatRoomController {

    // 不同聊天室的訂閱路徑
    private static final String GREETINGS_TOPIC_PREFIX = "/topic/chats/";
    @Autowired
    private ChatRoomService chatRoomService;

    // 訂閱路徑動態生成
    @MessageMapping("/hello/{userPairingHistoryId}")
    @SendTo(GREETINGS_TOPIC_PREFIX + "{userPairingHistoryId}") // 動態生成路徑
    public Message sendMessageToSpecificRoom(@DestinationVariable String userPairingHistoryId, Message message) {
        Message savedMessage = chatRoomService.saveMessage(message);
        return savedMessage;
    }

    @GetMapping("/api/1.0/messages")
    public ResponseEntity<?> getChatroomMessage(@RequestParam("userPairingHistoryId") Integer userPairingHistoryId) {
        List<Message> retrieveMessage = chatRoomService.retrieveMessage(userPairingHistoryId);
        return ResponseEntity.ok(retrieveMessage);
    }


//    @GetMapping("/details")
//    @ResponseBody
//    public ResponseEntity<?> getArticleDetails(@RequestParam("id") String id) {
//        Article article = articleService.getArticleById(id);
//        if (article == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Article not found"));
//        }
//        return ResponseEntity.ok(article);
//    }

}


