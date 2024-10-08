package com.example.demo.controller;

import com.example.demo.dto.LikeRequest;
import com.example.demo.model.article.Article;
import com.example.demo.model.article.ArticleComment;
import com.example.demo.model.article.Category;
import com.example.demo.model.user.User;
import com.example.demo.model.user.UserClickDetail;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.repository.UserClickDetailRepository;
import com.example.demo.service.ArticleService;
import com.example.demo.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/1.0/articles")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private final ArticleService articleService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserClickDetailRepository userClickDetailRepository;
    @Value("${jwt.secret}")
    private String jwtSecret;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // Guests can read all articles (If no token)
    @GetMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllArticles(@RequestParam(value = "paging", defaultValue = "0") int paging) {
        int pageSize = 10;
        // 10 uncategorized article data
        List<Article> articleList = articleService.getAllArticles(paging, pageSize);

        for (Article article : articleList) {
            String nickname = article.getUserNickname();
            article.setNickname(nickname);
        }

        // Calculate total pages
        int totalArticles = articleService.countTotalArticles();
        int totalPages = (int) Math.ceil((double) totalArticles / pageSize);

        // Return results, including article lists and total pages
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articleList);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/algo")
    @ResponseBody
    public ResponseEntity<?> getAllArticlesByAlgo(@RequestParam(value = "paging", defaultValue = "0") int paging, @RequestHeader(value = "Authorization") String jwtToken) {
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bearer token is missing"));
        }

        // parse user ID from JWT token
        String token = jwtToken.substring(7); // Remove "Bearer " prefix

        // Parse the JWT token to extract user information
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        // Get user ID from JWT token
        Integer userId = Integer.parseInt(claims.getSubject()); // Assuming subject is user ID

        UserClickDetail userClickDetail = userClickDetailRepository.findByUserId(userId).orElse(new UserClickDetail());
        Category favoriteTopic = userClickDetail.getFavoriteTopic();
        Category recommendTopic1 = userClickDetail.getRecommendTopic1();
        Category recommendTopic2 = userClickDetail.getRecommendTopic2();


        int pageSize = 10;
        // According to user interests and recommended topics to search articles
        List<Article> articles = articleService.findArticlesByTopics(paging, favoriteTopic, recommendTopic1, recommendTopic2, pageSize);

        return ResponseEntity.ok(articles);
    }

    @GetMapping("/details")
    @ResponseBody
    public ResponseEntity<?> getArticleDetails(@RequestParam("id") String id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Article not found"));
        }
        return ResponseEntity.ok(article);
    }

    @GetMapping("/details/comments")
    @ResponseBody
    public ResponseEntity<?> getCommentsDetails(@RequestParam("id") String id) {
        List<ArticleComment> comment = articleService.getCommentByArticleId(id);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/details/comments")
    @ResponseBody
    public ResponseEntity<?> postCommentsDetails(@RequestBody ArticleComment articleComment, @RequestHeader(value = "Authorization") String jwtToken) {


        try {
            // Remove Bearer prefix and check if the token is present
            if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bearer token is missing"));
            }

            String token = jwtToken.substring(7); // Remove "Bearer " prefix

            // Parse the JWT token to extract user information
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            // Get user ID from JWT token
            Integer userId = Integer.parseInt(claims.getSubject()); // Assuming subject is user ID
            User user = userService.getUserById(userId);
            articleComment.setUserId(user);

            articleService.postComment(articleComment);
            Map<String, Object> response = new HashMap<>();
            response.put("data", articleComment);
            return ResponseEntity.ok(response);

        } catch (SignatureException ex) {
            // JWT invalid
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid JWT token"));
        } catch (JwtException ex) {
            // Other JWT error
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "JWT token error"));
        } catch (Exception ex) {
            // other error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred processing your request"));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<?> searchProductsByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "paging", defaultValue = "0") int paging) {

        // error 1: no keyword
        if (keyword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Keyword parameter is required"));
        }

        try {
            int pageSize = 10; // 10 articles every page
            List<Article> articleList = articleService.findArticlesByKeyword(keyword, paging, pageSize);

            // error 2: can't find products
            if (articleList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No articles found."));
            }

            // Return Array of Product Object
            Map<String, Object> response = new HashMap<>();
            response.put("data", articleList);


            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid paging value. Please ensure it's a valid integer."));
        } catch (Exception e) {
            // Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> postArticle(@RequestBody Article article, @RequestHeader(value = "Authorization") String jwtToken) {
        try {
            // Remove Bearer prefix and check if the token is present
            if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bearer token is missing"));
            }

            String token = jwtToken.substring(7); // Remove "Bearer " prefix

            // Parse the JWT token to extract user information
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            // Get user ID from JWT token
            Integer userId = Integer.parseInt(claims.getSubject()); // Assuming subject is user ID
            User user = userService.getUserById(userId);
            article.setUserId(user);

            articleService.postArticle(article);
            Map<String, Object> response = new HashMap<>();
            response.put("data", article);
            return ResponseEntity.ok(response);

        } catch (SignatureException ex) {
            // JWT invalid
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid JWT token"));
        } catch (JwtException ex) {
            // Other JWT error
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "JWT token error"));
        } catch (Exception ex) {
            // other error
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred processing your request"));
        }
    }

    /**
     * Retrieves all articles.
     *
     * @return A ResponseEntity containing a list of Article objects.
     */
    @GetMapping("/guest")
    @ResponseBody
    public ResponseEntity<?> getAllArticle() {
        List<Article> articles = articleService.getAllArticle();
        return ResponseEntity.ok(articles);
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeArticle(@RequestBody LikeRequest likeRequest) {
        System.out.println(likeRequest);
        Integer articleId = likeRequest.getArticleId();
        try {
            // call service
            articleService.toggleLike(articleId);
            return new ResponseEntity<>(HttpStatus.CREATED); // like success
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // like fail
        }
    }


    @PostMapping("/click-events")
    @ResponseBody
    public ResponseEntity<?> postClickEvent(@RequestBody UserClickEvent userClickEvent, @RequestHeader(value = "Authorization") String jwtToken) {

        // Remove Bearer prefix and check if the token is present
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bearer token is missing"));
        }

        String token = jwtToken.substring(7); // Remove "Bearer " prefix

        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        Integer userId = Integer.parseInt(claims.getSubject()); // Assuming subject is user ID
        userClickEvent.setUserId(userId);

        // Extracting foreign key values
        Integer categoryId = userClickEvent.getCategoryId().getId();
        Timestamp timestamp = userClickEvent.getTimestamp();

        Map<String, Object> clickEventLogData = new HashMap<>();
        clickEventLogData.put("userId", userId);
        clickEventLogData.put("categoryId", categoryId);
        clickEventLogData.put("timestamp", timestamp);

        logger.info("Click Event Log data: {}", clickEventLogData);

        articleService.postClickEvent(userClickEvent);

        return ResponseEntity.ok("ok");
    }

    // To test end point
    @PostMapping("/calculateRecommendTopic")
    public ResponseEntity<?> calculateRecommendTopic() {
        articleService.updateUserClickDetail();
        articleService.calculateCosineSimilarity();
        return ResponseEntity.ok("Recommendation calculation completed successfully.");
    }


    @PostMapping("/sumToBigTable")
    public ResponseEntity<?> sumToBigTable() {
        articleService.updateUserClickDetail();
        return ResponseEntity.ok("Data summed to big table successfully.");
    }
}
