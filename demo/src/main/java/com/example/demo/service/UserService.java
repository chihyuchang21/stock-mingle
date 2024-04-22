package com.example.demo.service;

import com.example.demo.dto.MatchFriendInfo;
import com.example.demo.model.user.User;
import com.example.demo.model.user.UserHashtag;
import com.example.demo.model.user.UserPairingHistory;
import com.example.demo.model.user.UserSimilarity;
import com.example.demo.repository.GenderRepository;
import com.example.demo.repository.UserHashtagRepository;
import com.example.demo.repository.UserPairingHistoryRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
//        logger.info("userHashtags from service" + userHashtags);
        for (UserHashtag userHashtag : userHashtags) {
            String hashtagName = userHashtag.getHashtag().getHashtagName();
            hashtags.add(hashtagName);
        }
        return hashtags;
    }


    public void savePairToDatabase(Integer userId1, Integer userId2) {
        // user1,2 跟 user2,1視為相同的配對
        if (userPairingHistoryRepository.existsByUser1IdAndUser2Id(userId2, userId1)) {
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

    /**
     * Retrieves the list of matched friends for the specified user on the current day.
     *
     * @param userId The ID of the user to retrieve matched friends for.
     * @return A stream of MatchFriendInfo objects representing the matched friends.
     */
//    public Stream<MatchFriendInfo> getTodayMatch(Integer userId) {
//        Timestamp timestamp = Timestamp.valueOf("2024-04-19 18:09:30");
//        List<UserPairingHistory> pairingHistories = userPairingHistoryRepository.findByUserIdAndTimestamp(userId, timestamp);
//        return pairingHistories.stream()
//                .map(pairingHistory -> {
//                    Integer otherUserId = (userId.equals(pairingHistory.getUser1Id())) ? pairingHistory.getUser2Id() : pairingHistory.getUser1Id();
//                    User otherUser = userRepository.findById(otherUserId).orElse(null);
//                    if (otherUser != null) {
//                        return new MatchFriendInfo(otherUser.getNickname(), otherUser.getImage());
//                    }
//                    return null;
//                });
//    }
    public Stream<MatchFriendInfo> getTodayMatch(Integer userId) {
        List<UserPairingHistory> allPairingHistories = userPairingHistoryRepository.findAll();

        // flat map將每個元素都映射為element
        return allPairingHistories.stream()
                .flatMap(pairingHistory -> {
                    if (pairingHistory.getUser1Id().equals(userId) || pairingHistory.getUser2Id().equals(userId)) {
                        Integer otherUserId = (pairingHistory.getUser1Id().equals(userId)) ? pairingHistory.getUser2Id() : pairingHistory.getUser1Id();
                        User otherUser = userRepository.findById(otherUserId).orElse(null);
                        if (otherUser != null) {
                            return Stream.of(new MatchFriendInfo(otherUser.getNickname(), otherUser.getImage(), pairingHistory.getId())); // 添加了 pairingHistoryId
                        }
                    }
                    return Stream.empty();
                });
    }


    // Using AWS Lambda to execute the function once again at every midnight (start from day 2)
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSaveAfterDay2() {
        List<User> users = getAllUsers();
        List<UserPairingHistory> historyUserPairing = getHistoryUserPairing();

        Set<Integer> alreadyPairedUsersToday = new HashSet<>();
        List<Pair<Integer, Integer>> historyPairedUsers = new ArrayList<>();
        for (UserPairingHistory pairing : historyUserPairing) {
            Pair<Integer, Integer> pair = Pair.of(pairing.getUser1Id(), pairing.getUser2Id());
            historyPairedUsers.add(pair);
        }

        List<UserSimilarity> similarities = new ArrayList<>();
        Set<Integer> historypairedUserIds = new HashSet<>();

        for (User user : users) {
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            Double maxSimilarity = Double.MIN_VALUE;

            for (User otherUser : users) {
                if (user.getId().equals(otherUser.getId()) || alreadyPairedUsersToday.contains(userId1)) {
                    continue; // 跳過自己或已參與配對的用戶
                }
                Double similarity = calculateSimilarity(userId1, otherUser.getId());
                if ((user.getGenderMatch() == otherUser.getGenderId() || otherUser.getGenderMatch() == user.getGenderId())
                        && similarity > maxSimilarity && !isUserPaired(otherUser.getId(), historypairedUserIds)
                        && !isAlreadyPaired(userId1, otherUser.getId(), historyPairedUsers)) {
                    mostSimilarUserId = otherUser.getId();
                    maxSimilarity = similarity;
                }
            }

            if (mostSimilarUserId != null) {
                Pair<Integer, Integer> currentPair = Pair.of(Math.min(userId1, mostSimilarUserId), Math.max(userId1, mostSimilarUserId));
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
                savePairToDatabase(userId1, mostSimilarUserId);
                historypairedUserIds.add(userId1);
                historypairedUserIds.add(mostSimilarUserId);
                alreadyPairedUsersToday.add(userId1);
                alreadyPairedUsersToday.add(mostSimilarUserId);
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

    private boolean isAlreadyPaired(Integer userId1, Integer userId2, List<Pair<Integer, Integer>> historyPairedUsers) {
        Pair<Integer, Integer> pair = Pair.of(Math.min(userId1, userId2), Math.max(userId1, userId2));
        return historyPairedUsers.contains(pair);
    }

    // Using AWS Lambda to execute the function once again at every midnight (day 1)
    public List<UserSimilarity> calculateAllUsersSimilarityAndSaveDay1() {
        List<User> users = getAllUsers();
        List<UserSimilarity> similarities = new ArrayList<>();
        Set<Integer> pairedUserIds = new HashSet<>();
        Set<Integer> alreadyPairedUsers = new HashSet<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            Double maxSimilarity = Double.MIN_VALUE;

            for (int j = 0; j < users.size(); j++) {
                if (i == j || alreadyPairedUsers.contains(userId1)) {
                    continue;
                }
                User otherUser = users.get(j);
                Double similarity = calculateSimilarity(userId1, otherUser.getId());
                if ((user.getGenderMatch() == otherUser.getGenderId() || otherUser.getGenderMatch() == user.getGenderId())
                        && similarity > maxSimilarity && !isUserPaired(otherUser.getId(), pairedUserIds)) {
                    mostSimilarUserId = otherUser.getId();
                    maxSimilarity = similarity;
                }
            }

            if (mostSimilarUserId != null) {
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
                savePairToDatabase(userId1, mostSimilarUserId);
                pairedUserIds.add(userId1);
                pairedUserIds.add(mostSimilarUserId);
                alreadyPairedUsers.add(userId1);
                alreadyPairedUsers.add(mostSimilarUserId);
            }
        }

        return similarities;
    }


    @Scheduled(cron = "0 6 13 * * *")
    public void testScheduled() {
        logger.info("Scheduled Function Tested");
    }
}

