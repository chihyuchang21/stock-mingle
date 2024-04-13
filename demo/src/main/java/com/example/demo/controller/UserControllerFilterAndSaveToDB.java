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
        // Set to track users already involved in a pairing
        Set<Integer> alreadyPairedUsers = new HashSet<>();

        // For-each loop: looping over User objects in users
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            Double maxSimilarity = Double.MIN_VALUE;

            // 找目前相似度最高的用戶
            for (int j = 0; j < users.size(); j++) {
                if (i == j || alreadyPairedUsers.contains(userId1)) {
                    continue; // 跳過自己或已參與配對的使用者
                }
                User otherUser = users.get(j);
                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
                if (similarity > maxSimilarity && !isUserPaired(otherUser.getId(), pairedUserIds)) {
                    mostSimilarUserId = otherUser.getId();
                    maxSimilarity = similarity;
                }
            }

            // Store the pair if it's not null
            if (mostSimilarUserId != null) {
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
                userService.savePairToDatabase(userId1, mostSimilarUserId);
                // Mark both users as paired
                pairedUserIds.add(userId1);
                pairedUserIds.add(mostSimilarUserId);
                alreadyPairedUsers.add(userId1);
                alreadyPairedUsers.add(mostSimilarUserId);
            }
        }

        return ResponseEntity.ok(similarities);
    }

    // Check if a user is already paired
    private boolean isUserPaired(Integer userId, Set<Integer> pairedUserIds) {
        for (Integer pairedUserId : pairedUserIds) {
            if (pairedUserId.equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
