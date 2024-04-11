package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository){
        this.articleRepository = articleRepository;
    }

    public void postArticle(Article article){
        articleRepository.save(article);
    }

    public List<Article> getAllArticle(){
        return articleRepository.findAll();
    }
}
