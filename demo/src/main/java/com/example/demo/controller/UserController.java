package com.example.demo.controller;

import com.example.demo.middleware.JwtTokenService;
import com.example.demo.model.user.LoginRequest;
import com.example.demo.model.user.User;
import com.example.demo.model.user.UserPairingHistory;
import com.example.demo.model.user.UserSimilarity;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/1.0/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 使用Constructor注入
    public UserController(UserService userService,
                          JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody User user) {
        // Regular expression for validating email

        // Check for null or empty fields
        if (user.getAccountName() == null || user.getNickname().isEmpty() || user.getPassword() == null || user.getGenderId() == null || user.getGenderMatch() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Please fill all information"));
        }

        // Convert genderId and genderMatch to Gender objects

        // True: Email not exists
        boolean registrationSuccess = userService.registerUser(user);
        if (!registrationSuccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Account name already exists"));
        }

        String accessToken = jwtTokenService.generateAccessToken(user);
        long accessExpired = 3600;

        // Construct User Information
        Map<String, Object> userMap = new HashMap<>();

        // Return Auto-increment ID
        Integer autoID = userService.getUserIdByAccountName(user.getAccountName());
        userMap.put("id", autoID);
        userMap.put("account_name", user.getAccountName());
        userMap.put("nickname", user.getNickname());
        userMap.put("image", user.getImage()); // assumed url

        // Construct Full Response
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("access_token", accessToken);
        dataMap.put("access_expired", accessExpired);
        dataMap.put("user", userMap);
        responseMap.put("data", dataMap);

        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody Map<String, Object> payload) {
        LoginRequest loginRequest = new ObjectMapper().convertValue(payload, LoginRequest.class);

        if (loginRequest.getAccountName().isEmpty() || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email, password and provider are required"));
        }

        User user = userService.authenticateUser(loginRequest.getAccountName(), loginRequest.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }

        String accessToken = jwtTokenService.generateAccessToken(user);

        return generateLogInResponse(user, accessToken);
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> getUserProfile(@RequestHeader(name = "Authorization") String authorizationHeader) {
        // Postman: choose type and add "Header " before the token
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // error (401): no token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Can't find token"));
        }
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

            // Parse the JWT token to extract user information
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            Map<String, Object> userClaims = claims.get("user", Map.class);
            userClaims.remove("id");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", userClaims);

            return ResponseEntity.ok().body(responseMap);
        } catch (SignatureException e) {
            // error (403): wrong token
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid Token"));
        } catch (Exception e) {
            // error (500): server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Can't find token"));
        }
    }


    private ResponseEntity<?> generateLogInResponse(User user, String accessToken) {
        long accessExpired = 3600;

        Map<String, Object> userMap = new HashMap<>();
        Integer autoID = userService.getUserIdByAccountName(user.getAccountName());
        userMap.put("id", autoID);
        userMap.put("account_name", user.getAccountName());
        userMap.put("nickname", user.getNickname());
        userMap.put("image", user.getImage());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("access_token", accessToken);
        dataMap.put("access_expired", accessExpired);
        dataMap.put("user", userMap);

        return ResponseEntity.ok().body(Map.of("data", dataMap));
    }


    // 待refactor階段再改
    @GetMapping("similarity/dayOtherThan1")
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSaveDay2() {
        List<User> users = userService.getAllUsers();
        List<UserPairingHistory> historyUserPairing = userService.getHistoryUserPairing();
        logger.info("historyUserPairing: " + historyUserPairing);

        Set<Integer> alreadyPairedUsersToday = new HashSet<>(); //To-do: rename //Perhaps having bugs

        List<Pair<Integer, Integer>> historyPairedUsers = new ArrayList<>();
        for (UserPairingHistory pairing : historyUserPairing) {
            Pair<Integer, Integer> pair = Pair.of(pairing.getUser1Id(), pairing.getUser2Id());
            historyPairedUsers.add(pair);
        }
        logger.info("historyPairedUsers: " + historyPairedUsers);

        List<UserSimilarity> similarities = new ArrayList<>();
        Set<Integer> historypairedUserIds = new HashSet<>();
        for (User user : users) {
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            Double maxSimilarity = Double.MIN_VALUE;

            for (User otherUser : users) {
                if (user.getId().equals(otherUser.getId()) || alreadyPairedUsersToday.contains(userId1)) { //Perhaps having bugs
                    continue; // 跳過自己或已參與配對的用戶
                }
                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
                if ((user.getGenderMatch() == otherUser.getGenderId() || otherUser.getGenderMatch() == user.getGenderId())
                        && similarity > maxSimilarity && !isUserPaired(otherUser.getId(), historypairedUserIds)) {
                    mostSimilarUserId = otherUser.getId();
                    maxSimilarity = similarity;
                }
            }

            // 如果找到了相似的用户，且非歷史配對，則保存到DB
            if (mostSimilarUserId != null) {
                Pair<Integer, Integer> currentPair = Pair.of(Math.min(userId1, mostSimilarUserId), Math.max(userId1, mostSimilarUserId));
                if (historyPairedUsers.contains(currentPair)) {
                    continue; // 如果已存在這個配對，則跳過
                }
                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
                userService.savePairToDatabase(userId1, mostSimilarUserId);
                historypairedUserIds.add(userId1);
                historypairedUserIds.add(mostSimilarUserId);
                alreadyPairedUsersToday.add(userId1);
                alreadyPairedUsersToday.add(mostSimilarUserId);

            }
        }

        return ResponseEntity.ok(similarities);
    }


    @GetMapping("/similarity/day1")
    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSave() {
        List<User> users = userService.getAllUsers();
        List<UserSimilarity> similarities = new ArrayList<>();
        // Set to store the already paired user IDs
        Set<Integer> pairedUserIds = new HashSet<>();
        // Set to track users already involved in a pairing
        Set<Integer> alreadyPairedUsers = new HashSet<>();

        // For-each loop: looping over User objects in users
        for (int i = 0; i < users.size(); i++) {
            logger.info("pairedUserIds: " + pairedUserIds);
            logger.info("alreadyPairedUsers: " + alreadyPairedUsers);
            User user = users.get(i);
            Integer userId1 = user.getId();
            Integer mostSimilarUserId = null;
            Double maxSimilarity = Double.MIN_VALUE; //If set null -> get error

            // 找目前相似度最高的用戶
            for (int j = 0; j < users.size(); j++) {
                if (i == j || alreadyPairedUsers.contains(userId1)) {
                    continue; // 跳過自己或已參與配對的使用者 //跨日的話這裡要修
                }
                User otherUser = users.get(j);
                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
                // 性別相符最相似者配對
                if ((user.getGenderMatch() == otherUser.getGenderId() || otherUser.getGenderMatch() == user.getGenderId())
                        && similarity > maxSimilarity && !isUserPaired(otherUser.getId(), pairedUserIds)) {
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
