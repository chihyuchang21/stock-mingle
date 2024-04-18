package com.example.demo.controller;

import com.example.demo.model.article.Article;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.service.ArticleService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Value("${jwt.secret}")
    private String jwtSecret;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Article>> getArticlesByPageAndFavoriteTopic(@RequestParam(value = "paging", defaultValue = "0") int paging) {
        int pageSize = 10;
        // 10 uncategorized article data
        List<Article> articleList = articleService.getArticlesByPageAndFavoriteTopic(paging, pageSize);
        // boolean hasMore = articleService.hasNextPage();


        return ResponseEntity.ok(articleList);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> postArticle(@RequestBody Article article, @RequestHeader(value = "Authorization") String jwtToken) {
        try {
            // Remove Bearer prefix and check if the token is present
            if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bearer token is missing"));
            }

//            String token = jwtToken.substring(7); // Remove "Bearer " prefix
//
//            // Parse the JWT token to extract user information
//            Claims claims = Jwts.parser()
//                    .setSigningKey(jwtSecret)
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            Map<String, Object> userClaims = claims.get("user", Map.class);
//            userClaims.remove("id");
//
//            Map<String, Object> responseMap = new HashMap<>();
//            responseMap.put("data", userClaims);

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
    public ResponseEntity<?> postClickEvent(@RequestBody UserClickEvent userClickEvent) {
        // Extracting foreign key values
        Integer categoryId = userClickEvent.getCategoryId().getId();
        Timestamp timestamp = userClickEvent.getTimestamp();

        Map<String, Object> clickEventLogData = new HashMap<>();
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

//    @GetMapping("/calculateRecommendTopic")
//    public ResponseEntity<?> calculateRecommendTopic() {
//        articleService.updateUserClickDetail();
//        articleService.calculateCosineSimilarity();
//        return ResponseEntity.ok("OK");
//    }

//
//    @GetMapping("/sumToBigTable")
//    public ResponseEntity<?> sumToBigTable() {
//        articleService.updateUserClickDetail();
//        return ResponseEntity.ok("OK");
//    }
}
