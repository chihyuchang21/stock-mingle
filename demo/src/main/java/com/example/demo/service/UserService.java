package com.example.demo.service;

import com.example.demo.dto.MatchFriendInfo;
import com.example.demo.model.user.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
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
    private HashtagRepository hashtagRepository;
    @Autowired
    private GenderRepository genderRepository;
    @Autowired
    private S3Uploader s3Uploader;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserPairingHistory> getHistoryUserPairing() {
        return userPairingHistoryRepository.findAll();
    }

    // For Sign-up
    public boolean registerUser(User user) {
        // Check if account name already exists
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


    // To return autoID to Controller
    public Integer getUserIdByAccountName(String accountName) {
        return userRepository.getUserIdByAccountName(accountName);
    }

    // Method to save user interest hashtags to the database
    public void saveUserHashtag(Integer userId, String hashtagName) {
        // Create a UserHashtag object and save it to the database
        UserHashtag userHashtag = new UserHashtag();
        System.out.println("HashtagName in Service:" + hashtagName);
        // First, query the corresponding User object by userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Then, query the corresponding Hashtag object by hashtagName
        Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                .orElseThrow(() -> new RuntimeException("Hashtag not found"));

        // Set the properties of the UserHashtag object according to user ID and interest hashtag
        userHashtag.setUser(user);
        userHashtag.setHashtag(hashtag);
        // Call the Repository to save it to the database
        userHashtagRepository.save(userHashtag);
    }

    // Save user image
    public void saveUserImage(MultipartFile image, HttpServletRequest request, Integer userId) {
        if (image != null && !image.isEmpty()) {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            logger.info("baseUrl: " + baseUrl);
            logger.info("userId: " + userId);
            String fileName = image.getOriginalFilename();

            // Save the file to S3
            saveMultipartFile(image, userId, baseUrl);

            String fileUrlSaved = "https://d1p236hm2ki7mx.cloudfront.net/uploads/" + "user" + userId + "-" + fileName;

            userRepository.updateUserImage(fileUrlSaved, userId);

            logger.info("https://d1p236hm2ki7mx.cloudfront.net/uploads/" + "user" + userId + "-" + fileName);
        }
    }

    public String saveMultipartFile(MultipartFile image, long userId, String baseUrl) {
        if (image != null && !image.isEmpty()) {
            try {
                // Path
                String baseDir = System.getProperty("user.dir");
                String relativeDir = "uploads/";
                String fileName = image.getOriginalFilename();
                String filePath = baseDir + File.separator + relativeDir + "user" + userId + "-" + fileName;

                logger.info("fileName: " + fileName);

                s3Uploader.saveMultipartFileToFile(image, "uploads/user" + userId + "-" + fileName);

                String imageUrl = baseUrl + "/" + relativeDir + "user" + userId + "-" + fileName;
                logger.info("imageUrl: " + imageUrl);
                return imageUrl;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public double calculateSimilarity(Integer userId1, Integer userId2) {
        Set<String> user1Hashtags = getUserHashtags(userId1);
        Set<String> user2Hashtags = getUserHashtags(userId2);
        return BagOfWordsAlgorithm.calculateSimilarity(user1Hashtags, user2Hashtags);
    }


    public Set<String> getUserHashtags(Integer userId) {
        Set<String> hashtags = new HashSet<>();
        List<UserHashtag> userHashtags = userHashtagRepository.findByUserId(userId);
        for (UserHashtag userHashtag : userHashtags) {
            String hashtagName = userHashtag.getHashtag().getHashtagName();
            hashtags.add(hashtagName);
        }
        return hashtags;
    }


    public void savePairToDatabase(Integer userId1, Integer userId2) {
        // Treat user1,2 and user2,1 as the same pairing
        if (userPairingHistoryRepository.existsByUser1IdAndUser2Id(userId2, userId1)) {
            return; // If the same pairing already exists in the database, do not save it
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // UserPairingHistory Object
        UserPairingHistory userPairingHistory = new UserPairingHistory();
        userPairingHistory.setUser1Id(userId1);
        userPairingHistory.setUser2Id(userId2);
        userPairingHistory.setTimestamp(timestamp);

        userPairingHistoryRepository.save(userPairingHistory);
    }


    /**
     * Retrieves the list of matched friends for the specified user on the current day.
     *
     * @param userId The ID of the user to retrieve matched friends for.
     * @return A stream of MatchFriendInfo objects representing the matched friends.
     */

    public Stream<MatchFriendInfo> getTodayMatch(Integer userId) {
        List<UserPairingHistory> allPairingHistories = userPairingHistoryRepository.findAll();

        // Use flatMap to map each element to a stream of elements
        return allPairingHistories.stream()
                .flatMap(pairingHistory -> {
                    if (pairingHistory.getUser1Id().equals(userId) || pairingHistory.getUser2Id().equals(userId)) {

                        Integer otherUserId = (pairingHistory.getUser1Id().equals(userId)) ? pairingHistory.getUser2Id() : pairingHistory.getUser1Id();
                        User otherUser = userRepository.findById(otherUserId).orElse(null);
                        if (otherUser != null) {
                            return Stream.of(new MatchFriendInfo(otherUser.getNickname(), otherUser.getImage(), pairingHistory.getId())); // Added pairingHistoryId
                        }
                    }
                    return Stream.empty();
                });
    }

    @Scheduled(cron = "0 40 23 * * ?") // Executes daily at 23:40
// Using AWS Lambda to execute the function again at midnight (starting from day 2)
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSaveAfterDay2() {
        logger.info("Executing function calculateAllUsersSimilarityAndSaveAfterDay2...");
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
                    continue; // Skip the user itself or users already paired today
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
                savePairToDatabase(userId1, mostSimilarUserId); // Save the pairing to the database
                historypairedUserIds.add(userId1);
                historypairedUserIds.add(mostSimilarUserId);
                alreadyPairedUsersToday.add(userId1);
                alreadyPairedUsersToday.add(mostSimilarUserId);
            }
        }
        logger.info("Finished execution of function calculateAllUsersSimilarityAndSaveAfterDay2...");
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

    public User getUserById(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }


    @Scheduled(cron = "0 6 13 * * *")
    public void testScheduled() {
        logger.info("Scheduled Function Tested");
    }
}

