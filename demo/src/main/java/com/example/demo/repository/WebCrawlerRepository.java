package com.example.demo.repository;

import com.example.demo.model.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebCrawlerRepository extends JpaRepository<Article, Long> {
}

