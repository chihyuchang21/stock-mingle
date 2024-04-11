package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.model.user.UserClick;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.UserClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository){
        this.articleRepository = articleRepository;
    }

    @Autowired
    private UserClickRepository userClickRepository;

    public void postArticle(Article article){
        articleRepository.save(article);
    }

    public List<Article> getAllArticle(){
        return articleRepository.findAll();
    }

public void calculateCosineSimilarity() {
    List<UserClick> userClicks = userClickRepository.findAll();

    // 每個話題的點擊次數
    double usClickLength = 0.0;
    double twClickLength = 0.0;
    double dtClickLength = 0.0;

    for (UserClick userClick : userClicks) {
        usClickLength += Math.pow(userClick.getUsClick(), 2);
        twClickLength += Math.pow(userClick.getTwClick(), 2);
        dtClickLength += Math.pow(userClick.getDtClick(), 2);
    }

    usClickLength = Math.sqrt(usClickLength);
    twClickLength = Math.sqrt(twClickLength);
    dtClickLength = Math.sqrt(dtClickLength);

    // 話題間的內積
    double usTwDotProduct = 0.0;
    double usDtDotProduct = 0.0;
    double twDtDotProduct = 0.0;

    for (UserClick userClick : userClicks) {
        usTwDotProduct += userClick.getUsClick() * userClick.getTwClick();
        usDtDotProduct += userClick.getUsClick() * userClick.getDtClick();
        twDtDotProduct += userClick.getTwClick() * userClick.getDtClick();
    }

    // 計算cosine similarity
    double usTwSimilarity = usTwDotProduct / (usClickLength * twClickLength);
    double usDtSimilarity = usDtDotProduct / (usClickLength * dtClickLength);
    double twDtSimilarity = twDtDotProduct / (twClickLength * dtClickLength);

    // 更新recommend topics
    for (UserClick userClick : userClicks) {
        String favoriteTopic = userClick.getFavoriteTopic();
        String recommendTopic1;
        String recommendTopic2;

        // 根據favorite_topic決定要比對哪個餘弦相似度
        switch (favoriteTopic) {
            case "us":
                recommendTopic1 = (usDtSimilarity > usTwSimilarity) ? "dt" : "tw";
                recommendTopic2 = (usDtSimilarity > usTwSimilarity) ? "tw" : "dt";
                break;
            case "tw":
                recommendTopic1 = (usTwSimilarity > twDtSimilarity) ? "us" : "dt";
                recommendTopic2 = (usTwSimilarity > twDtSimilarity) ? "dt" : "us";
                break;
            case "dt":
                recommendTopic1 = (usDtSimilarity > twDtSimilarity) ? "us" : "tw";
                recommendTopic2 = (usDtSimilarity > twDtSimilarity) ? "tw" : "us";
                break;
            default:
                recommendTopic1 = "";
                recommendTopic2 = "";
        }

        userClick.setRecommendTopic1(recommendTopic1);
        userClick.setRecommendTopic2(recommendTopic2);
        userClickRepository.save(userClick);
        }
    }
}
