package com.example.demo.controller;
import com.example.demo.model.article.Article;
import com.example.demo.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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


    //DB欄位要改名叫article
    @PostMapping("/postArticle")
    public ResponseEntity<?> postArticle(@RequestBody Article article){
        logger.info(article.toString());
        articleService.postArticle(article);
        Map<String, Object> response = new HashMap<>();
        response.put("data", article);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllArticle")
    public ResponseEntity<?> getAllArticle(){
        List<Article> articles = articleService.getAllArticle();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/calculateRecommendTopic")
    public ResponseEntity<?> calculateRecommendTopic(){
        articleService.calculateCosineSimilarity();
        return ResponseEntity.ok("OK");
    }
}
