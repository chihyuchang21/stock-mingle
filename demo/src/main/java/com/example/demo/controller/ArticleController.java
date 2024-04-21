package com.example.demo.controller;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import com.example.demo.model.user.UserClickDetail;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.repository.UserClickDetailRepository;
import com.example.demo.service.ArticleService;
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
    private UserClickDetailRepository userClickDetailRepository;
    @Value("${jwt.secret}")
    private String jwtSecret;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // Guests can read all of articles (If no token)
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Article>> getAllArticles(@RequestParam(value = "paging", defaultValue = "0") int paging) {
        int pageSize = 10;
        // 10 uncategorized article data
        List<Article> articleList = articleService.getAllArticles(paging, pageSize);
        // boolean hasMore = articleService.hasNextPage();
        return ResponseEntity.ok(articleList);
    }


    @GetMapping("/algo")
    @ResponseBody
    public ResponseEntity<?> getAllArticlesByAlgo(@RequestParam(value = "paging", defaultValue = "0") int paging, @RequestHeader(value = "Authorization") String jwtToken) {
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bearer token is missing"));
        }

//        try {
        // 從JWT token中解析用戶ID
        String token = jwtToken.substring(7); // Remove "Bearer " prefix

        // Parse the JWT token to extract user information
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        // Get user ID from JWT token
        Integer userId = Integer.parseInt(claims.getSubject()); // Assuming subject is user ID
        logger.info("userid: " + userId);

        UserClickDetail userClickDetail = userClickDetailRepository.findByUserId(userId).orElse(new UserClickDetail());
        ;

        Category favoriteTopic = userClickDetail.getFavoriteTopic();
        logger.info("favoriteTopic: " + favoriteTopic);
        Category recommendTopic1 = userClickDetail.getRecommendTopic1();
        logger.info("RecommendTopic1: " + recommendTopic1);
        Category recommendTopic2 = userClickDetail.getRecommendTopic2();
        logger.info("RecommendTopic2: " + recommendTopic2);

        // 從類別對象中獲取類別的ID
//        int favoriteTopicId = favoriteTopic.getId();
//        logger.info("favoriteTopicId: " + favoriteTopicId);
//        int recommendTopic1Id = recommendTopic1.getId();
//        int recommendTopic2Id = recommendTopic2.getId();

        int pageSize = 10;
        // 根據用戶的興趣和推薦主題查找相關的文章
        List<Article> articles = articleService.findArticlesByTopics(paging, favoriteTopic, recommendTopic1, recommendTopic2, pageSize);


        // Get articles
//        List<Article> articleList = articleService.getAllArticles(paging, pageSize);
        return ResponseEntity.ok(articles);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
//        }
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
            article.setUserId(userId);
            logger.info("userid: " + userId);

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


//    @GetMapping("/getArticlesByPageAndFavoriteTopic")
//    public ResponseEntity<List<Article>> getArticlesByPageAndFavoriteTopic(@RequestParam(value = "paging", defaultValue = "0") int paging) {
//
//        int pageSize = 10;
//        // 不分類的10筆文章資料
//        List<Article> articleList = articleService.getArticlesByPageAndFavoriteTopic(paging, pageSize);
//    //boolean hasMore = articleService.hasNextPage();
//
//
//
//        return ResponseEntity.ok(articleList);
//    }
//
//    @GetMapping("/calculateRecommendTopic")
//    public ResponseEntity<?> calculateRecommendTopic() {
//        articleService.updateUserClickDetail();
//        articleService.calculateCosineSimilarity();
//        return ResponseEntity.ok("OK");
//    }
//
//
//    @GetMapping("/sumToBigTable")
//    public ResponseEntity<?> sumToBigTable() {
//        articleService.updateUserClickDetail();
//        return ResponseEntity.ok("OK");
//    }
}
