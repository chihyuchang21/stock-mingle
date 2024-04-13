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


        // For-each loop: looping over User objects in users
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            // 不能設為 null
            Double maxSimilarity = Double.MIN_VALUE;

            // 找目前相似度最高的用戶
            for (int j = 0; j < users.size(); j++) {
                if (i == j) {
                    continue; // 跳過自己
                }
                User otherUser = users.get(j);
                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
                if (similarity > maxSimilarity) {
                    mostSimilarUserId = otherUser.getId();
                    maxSimilarity = similarity;
                }
            }


            // Store the pair if it's not null and not already paired
            if (mostSimilarUserId != null && !isPairAlreadyExists(similarities, userId1, mostSimilarUserId)) {
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
                userService.savePairToDatabase(userId1, mostSimilarUserId);
            }
        }

        return ResponseEntity.ok(similarities);
    }

    // Check if the pair already exists in the list
    private boolean isPairAlreadyExists(List<UserSimilarity> similarities, Integer userId1, Integer userId2) {
        for (UserSimilarity similarity : similarities) {
            if ((similarity.getUserId1().equals(userId1) && similarity.getUserId2().equals(userId2)) ||
                    (similarity.getUserId1().equals(userId2) && similarity.getUserId2().equals(userId1))) {
                return true;
            }
        }
        return false;
    }
}
