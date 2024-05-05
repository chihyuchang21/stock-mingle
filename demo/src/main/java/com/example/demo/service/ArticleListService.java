//package com.example.demo.service;
//
//import org.springframework.stereotype.Service;
//
//@Service
//public class ArticleListService {
//
////    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerController.class);
////
////    @Autowired
////    private ArticleRepository articleRepository;
////
////    public List<Article> findArticlesByCategoryName(String categoryName, int page, int pageSize) {
////        // 根據類別名稱查詢類別id
////        logger.info(categoryName);
//////        Category category = getCategoryByName(categoryName);
////
////        // 查詢分頁的文章
//////        Pageable pageable = PageRequest.of(page, pageSize);
//////        Page<Article> articlePage = articleRepository.findByCategory(category, pageable);
////////        return articlePage.getContent();
//////    }
////
//////    public Category getCategoryByName(String categoryName) {
//////        // 根據類別名稱查詢類別id，這裡假設類別名稱是唯一的
//////        // 在實際場景中，可以根據需求來設計更複雜的查詢邏輯
//////        // 這裡只是一個簡單的示例
//////        return articleRepository.findByCategoryIgnoreCase(categoryName);
//////    }
////    }
//}
