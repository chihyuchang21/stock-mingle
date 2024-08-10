package com.example.demo.controller;

import com.example.demo.dto.ChatRoomInfo;
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

    // Subscription paths for different chat rooms
    private static final String GREETINGS_TOPIC_PREFIX = "/topic/chats/";
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Autowired
    private ChatRoomService chatRoomService;

    // Dynamically generate subscription paths
    @MessageMapping("/hello/{userPairingHistoryId}")
    @SendTo(GREETINGS_TOPIC_PREFIX + "{userPairingHistoryId}") // Dynamically generate the path
    public Message sendMessageToSpecificRoom(@DestinationVariable String userPairingHistoryId, Message message) {
        Message savedMessage = chatRoomService.saveMessage(message);
        return savedMessage;
    }

    @GetMapping("/api/1.0/messages")
    public ResponseEntity<?> getChatroomMessage(@RequestParam("userPairingHistoryId") Integer userPairingHistoryId) {
        List<Message> retrieveMessage = chatRoomService.retrieveMessage(userPairingHistoryId);
        return ResponseEntity.ok(retrieveMessage);
    }


    @GetMapping("/api/1.0/messages/chatroom-nickname")
    public ResponseEntity<?> getChatroomInfo(@RequestHeader(name = "Authorization") String authorizationHeader) {
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

        // Call the service and pass the parsed ID to it (use Message for now)
        List<ChatRoomInfo> chatrooms = chatRoomService.getChatroomInfoByUserId(userId);

        // Return the list of chat rooms
        return ResponseEntity.ok(chatrooms);
    }
}


