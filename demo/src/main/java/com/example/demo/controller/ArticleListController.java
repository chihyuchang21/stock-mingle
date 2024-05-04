package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/1.0/articles")
public class ArticleListController {

    @GetMapping("/{categoryName}")
    public ResponseEntity<?> getArticlesByCategoryName(
            @PathVariable("categoryName") String categoryName,
            @RequestParam(value = "paging", defaultValue = "0") String pagingStr) {

        Map<String, Object> response = new HashMap<>();
        try {
            // This part perhaps should be deleted when building other parts
            List<String> allowedCategories = Arrays.asList("all", "company-news", "broad-market-news", "company-discussion", "advice-request", "others");
            if (!allowedCategories.contains(categoryName.toLowerCase())) {
                response.put("error", "Invalid category name.");
                return ResponseEntity.badRequest().body(response);
            }

            int paging = Integer.parseInt(pagingStr);
            if (paging < 0) {
                response.put("error", "Paging value must be non-negative.");
                return ResponseEntity.badRequest().body(response);
            }

            int pageSize = 10;

            // When categoryName "all" -> request all products
            if ("all".equalsIgnoreCase(categoryName)) {
                categoryName = ""; // set category as null means not selecting any category
            }

            List<Product> products = productListService.findProductsByCategoryName(categoryName, paging, pageSize);
            boolean hasMore = productListService.hasNextPageByName(categoryName, paging, pageSize);

            response.put("data", products);
            if (hasMore) {
                response.put("next_paging", paging + 1);
            }

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("error", "Invalid paging value. Please ensure it's a valid integer.");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
