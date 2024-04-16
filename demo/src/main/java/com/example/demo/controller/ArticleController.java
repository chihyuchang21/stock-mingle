package com.example.demo.controller;
import com.example.demo.model.article.Article;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.repository.UserClickDetailRepository;
import com.example.demo.repository.UserClickEventRepository;
import com.example.demo.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ArticleController {

    private final ArticleService articleService;



    public ArticleController(ArticleService articleService){
        this.articleService = articleService;
    }

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    UserClickEventRepository userClickEventRepository;

    @Autowired
    UserClickDetailRepository userClickDetailRepository;


    //DB欄位要改名叫article
    @PostMapping("/postArticle")
    public ResponseEntity<?> postArticle(@RequestBody Article article){
        logger.info(article.toString());
        articleService.postArticle(article);
        Map<String, Object> response = new HashMap<>();
        response.put("data", article);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/postClickEvent")
    public ResponseEntity<?> postClickEvent(@RequestBody UserClickEvent userClickEvent){
        //獲取外鍵的值
        Integer categoryId = userClickEvent.getCategoryId().getId();
        Timestamp timestamp = userClickEvent.getTimestamp();
        logger.info("categoryId: " + categoryId);
        logger.info("timestamp: " + timestamp);
        articleService.postClickEvent(userClickEvent);

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/getAllArticle")
    public ResponseEntity<?> getAllArticle(){
        List<Article> articles = articleService.getAllArticle();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/getArticlesByPageAndFavoriteTopic")
    public ResponseEntity<List<Article>> getArticlesByPageAndFavoriteTopic(@RequestParam(value = "paging", defaultValue = "0") int paging) {

        int pageSize = 10;
        // 不分類的10筆文章資料
        List<Article> articleList = articleService.getArticlesByPageAndFavoriteTopic(paging, pageSize);
//        boolean hasMore = articleService.hasNextPage();



        return ResponseEntity.ok(articleList);
    }

    //Perhaps should be adjusted to PostMapping since we change stuffs in DB
    @GetMapping("/calculateRecommendTopic")
    public ResponseEntity<?> calculateRecommendTopic(){
        articleService.calculateCosineSimilarity();
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/sumToBigTable")
    public ResponseEntity<?> sumToBigTable(){
        articleService.updateUserClickDetail();
        return ResponseEntity.ok("OK");
    }
}
