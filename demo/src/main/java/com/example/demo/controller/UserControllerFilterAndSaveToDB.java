package com.example.demo.controller;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserSimilarity;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UserControllerFilterAndSaveToDB {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserControllerFilterAndSaveToDB.class);

    @GetMapping("/similarity/filterAndSaveToDB")
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSave() {
        List<User> users = userService.getAllUsers();
        List<UserSimilarity> similarities = new ArrayList<>();

        // loop所有用戶
        for (User user : users) {
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            Double maxSimilarity = Double.MIN_VALUE;

            // 找目前相似度最高的用戶
            for (User otherUser : users) {
                if (user.getId().equals(otherUser.getId())) {
                    continue; // 跳過自己
                }
                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
                if (similarity > maxSimilarity) {
                    mostSimilarUserId = otherUser.getId();
                    maxSimilarity = similarity;
                }
            }

            // 存到DB中
            if (mostSimilarUserId != null) {
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId));
                userService.savePairToDatabase(userId1, mostSimilarUserId);
            }
        }

        return ResponseEntity.ok(similarities);
    }
}

