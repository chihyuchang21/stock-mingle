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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UserControllerFilterAndSaveToDB {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserControllerFilterAndSaveToDB.class);

    @GetMapping("/similarity/filterAndSaveToDB")
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSave() {
        List<User> users = userService.getAllUsers();
        List<UserSimilarity> similarities = new ArrayList<>();

        // Set to store the already paired user IDs
        Set<Integer> pairedUserIds = new HashSet<>();


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

            // Store the pair if it's not null and not already paired
            if (mostSimilarUserId != null && !pairedUserIds.contains(userId1) && !pairedUserIds.contains(mostSimilarUserId)) {
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
                userService.savePairToDatabase(userId1, mostSimilarUserId);
                // Mark both users as paired
                pairedUserIds.add(userId1);
                pairedUserIds.add(mostSimilarUserId);
            }

        }

        return ResponseEntity.ok(similarities);
    }

}

