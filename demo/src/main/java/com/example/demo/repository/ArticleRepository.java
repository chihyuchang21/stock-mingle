package com.example.demo.repository;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    @Query("SELECT a FROM Article a ORDER BY a.id ASC")
    List<Article> findAllArticlesByPage(Pageable pageable);
    //之後可以加orderby

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
