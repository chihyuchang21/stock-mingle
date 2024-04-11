package com.example.demo.service;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserHashtag;
import com.example.demo.repository.UserHashtagRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserHashtagRepository userHashtagRepository;

    @Autowired
    private UserRepository userRepository;

//    public double calculateSimilarity(Long userId1, Long userId2) {
//        Set<String> user1Hashtags = getUserHashtags(userId1);
//        Set<String> user2Hashtags = getUserHashtags(userId2);
//        return BagOfWordsAlgorithm.calculateSimilarity(user1Hashtags, user2Hashtags);
//    }
//
//    private Set<String> getUserHashtags(Long userId) {
//        Set<String> hashtags = new HashSet<>();
//        UserHashtag userHashtag = userHashtagRepository.findByUserId(userId);
//        if (userHashtag != null) {
//            hashtags.add(userHashtag.getHashtag1());
//            hashtags.add(userHashtag.getHashtag2());
//            hashtags.add(userHashtag.getHashtag3());
//        }
//        return hashtags;
//    }

    public List<User> getAllUsersWithoutFavoriteTopicAndImage() {
        return userRepository.getAllUsersWithoutFavoriteTopicAndImage();
    }

public double calculateSimilarity(Long userId1, Long userId2) {
    Set<String> user1Hashtags = getUserHashtags(userId1);
    Set<String> user2Hashtags = getUserHashtags(userId2);
    return BagOfWordsAlgorithm.calculateSimilarity(user1Hashtags, user2Hashtags);
}

    private Set<String> getUserHashtags(Long userId) {
        Set<String> hashtags = new HashSet<>();
        UserHashtag userHashtag = userHashtagRepository.findByUserId(userId);
        if (userHashtag != null) {
            hashtags.add(userHashtag.getHashtag1());
            hashtags.add(userHashtag.getHashtag2());
            hashtags.add(userHashtag.getHashtag3());
        }
        return hashtags;
    }

}

