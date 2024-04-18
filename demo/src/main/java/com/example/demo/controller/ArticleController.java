package com.example.demo.controller;

import com.example.demo.model.article.Article;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResponseEntity<?> postArticle(@RequestBody Article article) {
        articleService.postArticle(article);
        Map<String, Object> response = new HashMap<>();
        response.put("data", article);
        return ResponseEntity.ok(response);
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
