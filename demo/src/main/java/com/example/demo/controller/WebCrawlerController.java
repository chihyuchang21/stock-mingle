package com.example.demo.controller;

import com.example.demo.model.stock.StockInformation;
import com.example.demo.service.FredStockService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/1.0")
public class WebCrawlerController {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerController.class);
    private final WebCrawlerService webCrawlerService;
    private final FredStockService fredStockService;

    public WebCrawlerController(WebCrawlerService webCrawlerService, FredStockService fredStockService) {
        this.webCrawlerService = webCrawlerService;
        this.fredStockService = fredStockService;
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

    @GetMapping("/stock-info-to-front-end")
    public ResponseEntity<?> getStockInfoToFrontEnd() {
        try {
            List<StockInformation> stockInformationList = webCrawlerService.getStockInfoToFrontEnd();
            List<Map<String, String>> stockInfoMapList = new ArrayList<>();
            for (StockInformation stockInfo : stockInformationList) {
                Map<String, String> stockInfoMap = new HashMap<>();
                stockInfoMap.put("name", stockInfo.getName());
                stockInfoMap.put("value", stockInfo.getValue());
                stockInfoMapList.add(stockInfoMap);
            }
            return ResponseEntity.ok(stockInfoMapList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve stock general index: " + e.getMessage());
        }
    }


    @GetMapping("/stock-info-fred")
    @ResponseBody
    public ResponseEntity<?> getStockGeneralIndexFromFred() {
        try {
            fredStockService.getStockGeneralIndex();
            return ResponseEntity.ok("Stock Index Data saved to DB!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch stock general index: " + e.getMessage());
        }
    }

//    @GetMapping("/stock-info")
//    @ResponseBody
//    public ResponseEntity<?> retrieveStockGeneralIndex() {
//        try {
//            webCrawlerService.retrieveStockGeneralIndex();
//            return ResponseEntity.ok("Stock Index Data retrieved from DB!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve stock general index: " + e.getMessage());
//        }
//    }
}

