package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.MatchFriendInfo;
import com.example.demo.middleware.JwtTokenService;
import com.example.demo.model.user.User;
import com.example.demo.repository.UserPairingHistoryRepository;
import com.example.demo.service.FileService;
import com.example.demo.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/1.0/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final FileService fileService;

    private final UserPairingHistoryRepository userPairingHistoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 使用Constructor注入
    public UserController(UserService userService,
                          JwtTokenService jwtTokenService,
                          FileService fileService,
                          UserPairingHistoryRepository userPairingHistoryRepository) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.fileService = fileService;
        this.userPairingHistoryRepository = userPairingHistoryRepository;
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody User user) {

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

        System.out.println("user: " + user);
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

    @PostMapping("/signup/hashtag")
    public ResponseEntity<?> signUp(@RequestParam("accountName") String accountName,
                                    @RequestParam("image") MultipartFile image,
                                    @RequestParam("hashtags") String hashtags,
                                    HttpServletRequest request) {
//        String accountName = signupRequest.getAccountName();
//        List<String> hashtags = signupRequest.getHashtags();
//        MultipartFile userImage = signupRequest.getImage();

        List<String> hashtagList = Arrays.asList(hashtags.split(",")); // 將逗號分隔的String改為單層List<String>

        System.out.println("request: " + request);
        System.out.println("image:" + image);
        System.out.println("hashtags:" + hashtags);

        // 根據帳戶名查找用戶ID
        Integer userId = userService.getUserIdByAccountName(accountName);
        if (userId == null) {
            // 如果用戶不存在，返回錯誤信息
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

//        fileService.saveUserImage(userImage, request);

        // 保存興趣標籤到數據庫
        for (String hashtag : hashtagList) {
            System.out.println("hashtag" + hashtag);
            userService.saveUserHashtag(userId, hashtag);
        }

        userService.saveUserImage(image, request, userId);

        // 返回成功信息
        return ResponseEntity.ok("Hashtags saved successfully");
    }

//    @PostMapping("/signup/image")
//    public ResponseEntity<?> signUp(@RequestBody SignupRequest signupRequest) {
//        userService.
//
//        return ResponseEntity.ok("Image saved successfully");
//    }


    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest) {
//        LoginRequest loginRequest = new ObjectMapper().convertValue(payload, LoginRequest.class);

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
//            userClaims.remove("id");

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

    @GetMapping("/match-today")
    public ResponseEntity<?> getTodayMatch(@RequestHeader(name = "Authorization") String jwtToken) {
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            // error (401): no token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Can't find token"));
        }
        try {
            // Extract JWT token from the Authorization header
            String token = jwtToken.substring(7); // Remove "Bearer " prefix

            // Parse the JWT token to extract user information
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            // Get user ID from JWT token
            Integer userId = Integer.parseInt(claims.getSubject()); // Assuming subject is user ID

            // Use userService to get user's friends
            Stream<MatchFriendInfo> todayMatchPerson = userService.getTodayMatch(userId);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", todayMatchPerson);

            return ResponseEntity.ok().body(responseMap);
        } catch (SignatureException e) {
            // error (403): wrong token
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid Token"));
        } catch (Exception e) {
            // error (500): server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 先送 Integer 再改 String
//    @GetMapping("/user-pairing-history/{pairingId}")
//    public Integer getOtherUserId(@PathVariable Integer id) {
//        // 根據前端發送的id，動態確定是使用user1_id還是user2_id來查詢對應的user_id
//        UserPairingHistory userPairingHistory = userPairingHistoryRepository.findOtherUserId(id);
//        if (userPairingHistory != null) {
//            // 如果找到對應的記錄，則返回另一個用戶的id
//            if (userPairingHistory.getUser1Id().equals(id)) {
//                return userPairingHistory.getUser2Id();
//            } else {
//                return userPairingHistory.getUser1Id();
//            }
//        } else {
//            // 如果查詢結果為空，返回null或者其他預設值，這裡假設返回0
//            return 0;
//        }
//    }
//}


    // move to Service testing endpoint
//    @GetMapping("/refactor")
//    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSaveAfterDay2() {
//        return userService.calculateAllUsersSimilarityAndSaveAfterDay2();
//    }
//
//
//    // 待refactor階段再改
//    @GetMapping("similarity/dayOtherThan1")
//    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSaveDay2() {
//        List<User> users = userService.getAllUsers();
//        List<UserPairingHistory> historyUserPairing = userService.getHistoryUserPairing();
//
//        Set<Integer> alreadyPairedUsersToday = new HashSet<>();
//        List<Pair<Integer, Integer>> historyPairedUsers = new ArrayList<>();
//        for (UserPairingHistory pairing : historyUserPairing) {
//            Pair<Integer, Integer> pair = Pair.of(pairing.getUser1Id(), pairing.getUser2Id());
//            historyPairedUsers.add(pair);
//        }
//
//        List<UserSimilarity> similarities = new ArrayList<>();
//        Set<Integer> historypairedUserIds = new HashSet<>();
//
//        for (User user : users) {
//            Integer userId1 = user.getId();
//            Integer mostSimilarUserId = null;
//            Double maxSimilarity = Double.MIN_VALUE;
//
//            for (User otherUser : users) {
//                if (user.getId().equals(otherUser.getId()) || alreadyPairedUsersToday.contains(userId1)) {
//                    continue; // 跳過自己或已參與配對的用戶
//                }
//                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
//                if ((user.getGenderMatch() == otherUser.getGenderId() || otherUser.getGenderMatch() == user.getGenderId())
//                        && similarity > maxSimilarity && !isUserPaired(otherUser.getId(), historypairedUserIds)
//                        && !isAlreadyPaired(userId1, otherUser.getId(), historyPairedUsers)) {
//                    mostSimilarUserId = otherUser.getId();
//                    maxSimilarity = similarity;
//                }
//            }
//
//            if (mostSimilarUserId != null) {
//                Pair<Integer, Integer> currentPair = Pair.of(Math.min(userId1, mostSimilarUserId), Math.max(userId1, mostSimilarUserId));
//                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
//                userService.savePairToDatabase(userId1, mostSimilarUserId);
//                historypairedUserIds.add(userId1);
//                historypairedUserIds.add(mostSimilarUserId);
//                alreadyPairedUsersToday.add(userId1);
//                alreadyPairedUsersToday.add(mostSimilarUserId);
//            }
//        }
//
//        return ResponseEntity.ok(similarities);
//    }

//    private boolean isAlreadyPaired(Integer userId1, Integer userId2, List<Pair<Integer, Integer>> historyPairedUsers) {
//        Pair<Integer, Integer> pair = Pair.of(Math.min(userId1, userId2), Math.max(userId1, userId2));
//        return historyPairedUsers.contains(pair);
//    }


//    @GetMapping("/similarity/day1")
//    public ResponseEntity<List<UserSimilarity>> calculateAllUsersSimilarityAndSave() {
//        List<User> users = userService.getAllUsers();
//        List<UserSimilarity> similarities = new ArrayList<>();
//        // Set to store the already paired user IDs
//        Set<Integer> pairedUserIds = new HashSet<>();
//        // Set to track users already involved in a pairing
//        Set<Integer> alreadyPairedUsers = new HashSet<>();
//
//        // For-each loop: looping over User objects in users
//        for (int i = 0; i < users.size(); i++) {
////            logger.info("pairedUserIds: " + pairedUserIds);
////            logger.info("alreadyPairedUsers: " + alreadyPairedUsers);
//            User user = users.get(i);
//            Integer userId1 = user.getId();
//            Integer mostSimilarUserId = null;
//            Double maxSimilarity = Double.MIN_VALUE; //If set null -> get error
//
//            // 找目前相似度最高的用戶
//            for (int j = 0; j < users.size(); j++) {
//                if (i == j || alreadyPairedUsers.contains(userId1)) {
//                    continue; // 跳過自己或已參與配對的使用者 //跨日的話這裡要修
//                }
//                User otherUser = users.get(j);
//                Double similarity = userService.calculateSimilarity(userId1, otherUser.getId());
//                // 性別相符最相似者配對
//                if ((user.getGenderMatch() == otherUser.getGenderId() || otherUser.getGenderMatch() == user.getGenderId())
//                        && similarity > maxSimilarity && !isUserPaired(otherUser.getId(), pairedUserIds)) {
//                    mostSimilarUserId = otherUser.getId();
//                    maxSimilarity = similarity;
//                }
//            }
//
//            // Store the pair if it's not null
//            if (mostSimilarUserId != null) {
//                similarities.add(new UserSimilarity(userId1, mostSimilarUserId, maxSimilarity));
//                userService.savePairToDatabase(userId1, mostSimilarUserId);
//                // Mark both users as paired
//                pairedUserIds.add(userId1);
//                pairedUserIds.add(mostSimilarUserId);
//                alreadyPairedUsers.add(userId1);
//                alreadyPairedUsers.add(mostSimilarUserId);
//            }
//        }
//
//        return ResponseEntity.ok(similarities);
//    }
//
//    // Check if a user is already paired
//    private boolean isUserPaired(Integer userId, Set<Integer> pairedUserIds) {
//        for (Integer pairedUserId : pairedUserIds) {
//            if (pairedUserId.equals(userId)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
