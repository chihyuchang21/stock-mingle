package com.example.demo.service;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserHashtag;
import com.example.demo.model.user.UserPairingHistory;
import com.example.demo.repository.GenderRepository;
import com.example.demo.repository.UserHashtagRepository;
import com.example.demo.repository.UserPairingHistoryRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserPairingHistoryRepository userPairingHistoryRepository;
    @Autowired
    private UserHashtagRepository userHashtagRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenderRepository genderRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserPairingHistory> getHistoryUserPairing() {
        return userPairingHistoryRepository.findAll();
    }

    // For Sign-up
    public boolean registerUser(User user) {
        //Check if account name already exists
        if (userRepository.existsByAccountName(user.getAccountName())) {
            return false;   // Account name already exists
        }

        // Hashed Password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);  //Before entering DB, pwd should be hashed
        user.setImage("default");

        // Save User Information to DB
        userRepository.save(user);
        return true;
    }

    public User authenticateUser(String accountName, String rawPassword) {
        User user = userRepository.getUserByAccountName(accountName);
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }


    //To return autoID to Controller
    public Integer getUserIdByAccountName(String accountName) {
        return userRepository.getUserIdByAccountName(accountName);
    }


    public double calculateSimilarity(Integer userId1, Integer userId2) {
        Set<String> user1Hashtags = getUserHashtags(userId1);
        Set<String> user2Hashtags = getUserHashtags(userId2);
        return BagOfWordsAlgorithm.calculateSimilarity(user1Hashtags, user2Hashtags);
    }


    public Set<String> getUserHashtags(Integer userId) {
        Set<String> hashtags = new HashSet<>();
        List<UserHashtag> userHashtags = userHashtagRepository.findByUserId(userId);
        logger.info("userHashtags from service" + userHashtags);
        for (UserHashtag userHashtag : userHashtags) {
            String hashtagName = userHashtag.getHashtag().getHashtagName();
            hashtags.add(hashtagName);
        }
        return hashtags;
    }


    public void savePairToDatabase(Integer userId1, Integer userId2) {
        // user1,2 跟 user2,1視為相同的配對
        if (userPairingHistoryRepository.existsByUser1IdAndUser2Id(userId2, userId1)) {
            logger.info("Pairing already exists for users: " + userId1 + " and " + userId2);
            return; // 如若DB中有已經有相同配對則不儲存
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // UserPairingHistory Object
        UserPairingHistory userPairingHistory = new UserPairingHistory();
        userPairingHistory.setUser1Id(userId1);
        userPairingHistory.setUser2Id(userId2);
        userPairingHistory.setTimestamp(timestamp);

        // 要用UserPairingRepo
        userPairingHistoryRepository.save(userPairingHistory);
    }

    public List<UserPairingHistory> getTodayMatch(Integer userId) {
        Timestamp timestamp = Timestamp.valueOf("2024-04-17 10:34:32");
        userRepository.logQueryParameters(userId, timestamp);
        return userRepository.findByUserIdAndTimestamp(userId, timestamp);
    }


    @Scheduled(cron = "0 6 13 * * *")
    public void testScheduled() {
        logger.info("Scheduled Function Tested");
    }
}

