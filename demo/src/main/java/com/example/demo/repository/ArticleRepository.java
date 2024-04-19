package com.example.demo.repository;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    @Query("SELECT a FROM Article a ORDER BY a.id ASC")
    List<Article> findAllArticlesByPage(Pageable pageable);
    //之後可以加orderby

    @Query("SELECT a FROM Article a WHERE a.categoryId = :favoriteTopic OR a.categoryId = :recommendTopic1 OR a.categoryId = :recommendTopic2")
    List<Article> findAllArticlesByPageAndTopics(Category favoriteTopic, Category recommendTopic1, Category recommendTopic2, Pageable pageable);

}
