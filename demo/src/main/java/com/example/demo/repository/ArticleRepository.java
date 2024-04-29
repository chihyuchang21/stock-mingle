package com.example.demo.repository;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisCommandTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    static final Logger logger = LoggerFactory.getLogger(ArticleRepository.class);
//    //之後可以加orderby

    @Query("SELECT a FROM Article a ORDER BY a.id DESC")
    List<Article> findAllArticlesByPageOrder(Pageable pageable);

    // JPA不能用Autowired故使用參數傳遞此方法
    default List<Article> findAllArticlesByPage(Pageable pageable, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        String key = "articles";

        // 第一頁的文章才存cache
        boolean isFirstPage = pageable.getPageNumber() == 0;

        try {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            // Try to get articles data from redis
            if (isFirstPage && redisTemplate.hasKey(key)) {
                logger.info("Articles retrieved from cache.");
                String articlesJson = ops.get(key);
                return objectMapper.readValue(articlesJson, new TypeReference<List<Article>>() {
                });
            }
        } catch (RedisCommandTimeoutException e) {
            logger.warn("Redis command timeout: {}", e.getMessage());
            // Handle Redis command timeout scenario
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection fails: {}", e.getMessage());
            // Redis connection failed
        } catch (RedisSystemException e) {
            logger.warn("Redis system exception occurred: {}", e.getMessage());
            // This catches RedisSystemException, including those caused by Redis exceptions
        } catch (IOException e) {
            logger.warn("Error parsing articles from cache: {}", e.getMessage());
            // Redis JSON parsing failed
        } catch (Exception e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            // Other unexpected errors
        }

        logger.info("Articles retrieved from database.");
        // 使用Spring Data JPA的findAllArticlesByPage方法來從數據庫獲取文章
        List<Article> articles = findAllArticlesByPageOrder(pageable);

        // 添加info到每個文章對象中
        for (Article article : articles) {
            article.setNickname(article.getUserNickname());
            article.setCategoryId(article.getCategoryId());
            article.setLikeCount(article.getLikeCount());
            article.setCommentCount(article.getCommentCount());
        }

        try {
            // 只有在第一頁時才存儲到緩存中
            if (isFirstPage) {
                ValueOperations<String, String> opsForCache = redisTemplate.opsForValue();
                String articlesJson = objectMapper.writeValueAsString(articles);
                opsForCache.set(key, articlesJson);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error caching articles: {}", e.getMessage());
        } catch (RedisCommandTimeoutException e) {
            logger.warn("Redis command timeout: {}", e.getMessage());
            // Handle Redis command timeout scenario
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection fails: {}", e.getMessage());
            // Handle Redis connection failure
        } catch (RedisSystemException e) {
            logger.warn("Redis system exception occurred: {}", e.getMessage());
            // Handle system exceptions related to Redis
        } catch (Exception e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            // Handle other unexpected errors
        }

        return articles;
    }


    @Query("SELECT a FROM Article a WHERE a.categoryId = :favoriteTopic OR a.categoryId = :recommendTopic1 OR a.categoryId = :recommendTopic2")
    List<Article> findAllArticlesByPageAndTopics(Category favoriteTopic, Category recommendTopic1, Category recommendTopic2, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.categoryId = :favoriteTopic ORDER BY a.id ASC")
    List<Article> findFavoriteTopicArticles(Pageable pageable, @Param("favoriteTopic") Category favoriteTopic);

    @Query("SELECT a FROM Article a WHERE a.categoryId = :recommendTopic1 ORDER BY a.id ASC")
    List<Article> findRecommendTopic1Articles(Pageable pageable, @Param("recommendTopic1") Category recommendTopic1);

    @Query("SELECT a FROM Article a WHERE a.categoryId = :recommendTopic2 ORDER BY a.id ASC")
    List<Article> findRecommendTopic2Articles(Pageable pageable, @Param("recommendTopic2") Category recommendTopic2);

    Optional<Article> findById(int id);

}
