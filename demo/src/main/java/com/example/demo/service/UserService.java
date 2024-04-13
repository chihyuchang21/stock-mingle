package com.example.demo.service;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserHashtag;
import com.example.demo.model.user.UserPairingHistory;
import com.example.demo.repository.UserHashtagRepository;
import com.example.demo.repository.UserPairingHistoryRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserPairingHistoryRepository userPairingHistoryRepository;

    @Autowired
    private UserHashtagRepository userHashtagRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

//    public List<User> getAllUsersWithoutFavoriteTopicAndImage() {
//        return userRepository.getAllUsersWithoutFavoriteTopicAndImage();
//    }

    public double calculateSimilarity(Integer userId1, Integer userId2) {
        Set<String> user1Hashtags = getUserHashtags(userId1);
        Set<String> user2Hashtags = getUserHashtags(userId2);
        return BagOfWordsAlgorithm.calculateSimilarity(user1Hashtags, user2Hashtags);
    }

    private Set<String> getUserHashtags(Integer userId) {
        Set<String> hashtags = new HashSet<>();
        UserHashtag userHashtag = userHashtagRepository.findByUserId(userId);
        if (userHashtag != null) {
            hashtags.add(userHashtag.getHashtag1());
            hashtags.add(userHashtag.getHashtag2());
            hashtags.add(userHashtag.getHashtag3());
        }
        return hashtags;
    }

    public void savePairToDatabase(Integer userId1, Integer userId2) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // UserPairingHistory Object
        UserPairingHistory userPairingHistory = new UserPairingHistory();
        userPairingHistory.setUser1Id(userId1);
        userPairingHistory.setUser2Id(userId2);
        userPairingHistory.setTimestamp(timestamp);

        // 要用UserPairingRepo
        userPairingHistoryRepository.save(userPairingHistory);
    }

    @Scheduled(cron = "0 6 13 * * *" )
    public void testScheduled(){
        logger.info("Scheduled Function Tested");
    }
}

