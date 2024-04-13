package com.example.demo.controller;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserSimilarity;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;

@Controller
public class UserControllerPrintAndFilterController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserControllerPrintAndFilterController.class);

    //Testing (只能印出數字)
//    @GetMapping("/users/similarity")
//    public ResponseEntity<Double> getUserSimilarity(@RequestParam Long userId1, @RequestParam Long userId2) {
//        Double similarity = userService.calculateSimilarity(userId1, userId2);
//        return ResponseEntity.ok(similarity);
//    }

    @GetMapping("/similarity/printAndFilter")
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarity() {
//        List<User> users = userService.getAllUsersWithoutFavoriteTopicAndImage();
        List<User> users = userService.getAllUsers();
        List<UserSimilarity> similarities = new ArrayList<>();

//        for (int i = 0; i < users.size(); i++) {
//            for (int j = i + 1; j < users.size(); j++) {
//                Long userId1 = users.get(i).getId();
//                Long userId2 = users.get(j).getId();
//                Double similarity = userService.calculateSimilarity(userId1, userId2);
//                similarities.add(new UserSimilarity(userId1, userId2, similarity));
//            }
//        }

        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                User user1 = users.get(i);
                User user2 = users.get(j);

                logger.info("user1.getGenderMatch(): " + user1.getGenderMatch());
                logger.info("user2.getGenderId(): " + user2.getGenderId());
                logger.info("user2.getGenderMatch(): " + user2.getGenderMatch());
                logger.info("user1.getGenderId(): " + user1.getGenderId());

                if (user1.getGenderMatch() == user2.getGenderId() || user2.getGenderMatch() == user1.getGenderId()) {
                    Integer userId1 = user1.getId();
                    Integer userId2 = user2.getId();
                    Double similarity = userService.calculateSimilarity(userId1, userId2);
                    logger.info("similarity: " + similarity);
                    similarities.add(new UserSimilarity(userId1, userId2, similarity));
                }
            }
        }

        return ResponseEntity.ok(similarities);
    }
}

