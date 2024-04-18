package com.example.demo.controller;

import com.example.demo.service.WebCrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/1.0")
public class WebCrawlerController {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerController.class);
    private final WebCrawlerService webCrawlerService;

    public WebCrawlerController(WebCrawlerService webCrawlerService) {
        this.webCrawlerService = webCrawlerService;
    }

    @GetMapping("/reddit-article")
    @ResponseBody
    public ResponseEntity<?> getRedditArticles() {
        try {
            String subreddit = "stocks"; // Reddit Stock
            int limit = 100; // Amount of articles

            // Call RedditService to get articles
            webCrawlerService.getRedditArticles(subreddit, limit);

            // can use responseBody Print the retrieved article information


            return ResponseEntity.ok("Article Data saved to DB!"); //待刪

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch Reddit articles: " + e.getMessage());
        }
    }

    @GetMapping("/stock-info")
    @ResponseBody
    public ResponseEntity<?> getStockGeneralIndex() {
        try {
            webCrawlerService.getStockGeneralIndex();
            return ResponseEntity.ok("Stock Index Data saved to DB!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch stock general index: " + e.getMessage());
        }
    }
}

