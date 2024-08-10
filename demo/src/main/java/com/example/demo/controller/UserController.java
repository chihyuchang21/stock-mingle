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

        List<String> hashtagList = Arrays.asList(hashtags.split(",")); // 將逗號分隔的String改為單層List<String>

        System.out.println("request: " + request);
        System.out.println("image:" + image);
        System.out.println("hashtags:" + hashtags);

        // Search user ID according to accountName
        Integer userId = userService.getUserIdByAccountName(accountName);
        if (userId == null) {
            // If user does not exist, return error msg
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Save interest hashtag to DB
        for (String hashtag : hashtagList) {
            System.out.println("hashtag" + hashtag);
            userService.saveUserHashtag(userId, hashtag);
        }

        userService.saveUserImage(image, request, userId);

        // Return msg
        return ResponseEntity.ok("Hashtags saved successfully");
    }


    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest) {

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
}
