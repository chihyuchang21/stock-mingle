package com.example.demo.controller;

import com.example.demo.model.message.Message;
import com.example.demo.service.ChatRoomService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class ChatRoomController {

    // 不同聊天室的訂閱路徑
    private static final String GREETINGS_TOPIC_PREFIX = "/topic/chats/";
    @Value("${jwt.secret}")
    private String jwtSecret;
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

    // 前端送來userId，用此去查詢有聊天的聊天式號碼
    @GetMapping("/api/1.0/messages/chatroom")
    public ResponseEntity<?> getChatrooms(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // error (401): no token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Can't find token"));
        }
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

        // Parse the JWT token to extract user information
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        Map<String, Object> userClaims = claims.get("user", Map.class);
        Integer userId = Integer.parseInt(userClaims.get("id").toString()); // Assuming ID is an Integer

        // 調用服務並將解析的ID傳遞給它 (先用Message)
        List<Integer> chatroom = chatRoomService.getChatroomsByUserId(userId);

        // 返回聊天室列表
        return ResponseEntity.ok(chatroom);
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


