package com.example.demo.controller;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserSimilarity;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    //Testing (只能印出數字)
//    @GetMapping("/users/similarity")
//    public ResponseEntity<Double> getUserSimilarity(@RequestParam Long userId1, @RequestParam Long userId2) {
//        Double similarity = userService.calculateSimilarity(userId1, userId2);
//        return ResponseEntity.ok(similarity);
//    }

    @GetMapping("/similarity/all")
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarity() {
        List<User> users = userService.getAllUsersWithoutFavoriteTopicAndImage();
        List<UserSimilarity> similarities = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                Long userId1 = users.get(i).getId();
                Long userId2 = users.get(j).getId();
                Double similarity = userService.calculateSimilarity(userId1, userId2);
                similarities.add(new UserSimilarity(userId1, userId2, similarity));
            }
        }

        return ResponseEntity.ok(similarities);
    }

}

