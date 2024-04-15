package com.example.demo.repository;

import com.example.demo.model.article.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public  interface ArticleRepository extends JpaRepository<Article, Integer> {
    @Query("SELECT a FROM Article a ORDER BY a.id ASC")
    List<Article> findArticlesByPageAndFavoriteTopic(Pageable pageable);
    //之後可以加orderby

}
