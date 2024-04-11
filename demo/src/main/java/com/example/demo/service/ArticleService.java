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

        System.out.println("usClickLength(pow): " + usClickLength);
        System.out.println("twClickLength(pow): " + twClickLength);
        System.out.println("dtClickLength(pow): " + dtClickLength);

        usClickLength = Math.sqrt(usClickLength);
        twClickLength = Math.sqrt(twClickLength);
        dtClickLength = Math.sqrt(dtClickLength);

        System.out.println("usClickLength(sqrt): " + usClickLength);
        System.out.println("twClickLength(sqrt): " + twClickLength);
        System.out.println("dtClickLength(sqrt): " + dtClickLength);


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

        // 結果
        System.out.println("餘弦相似度(US, TW): " + usTwSimilarity);
        System.out.println("餘弦相似度(US, DT): " + usDtSimilarity);
        System.out.println("餘弦相似度(TW, DT): " + twDtSimilarity);
    }
}
