package com.example.demo.controller;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users/similarity")
    public ResponseEntity<Double> getUserSimilarity(@RequestParam Long userId1, @RequestParam Long userId2) {
        Double similarity = userService.calculateSimilarity(userId1, userId2);
        return ResponseEntity.ok(similarity);
    }

}

