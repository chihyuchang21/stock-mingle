package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import com.example.demo.model.user.UserClickDetail;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);
    private final ArticleRepository articleRepository;
    @Autowired
    private UserClickRepository userClickRepository;
    @Autowired
    private UserClickEventRepository userClickEventRepository;
    @Autowired
    private UserClickDetailRepository userClickDetailRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public void postArticle(Article article) {
        articleRepository.save(article);
    }

    public List<Article> getAllArticle() {
        return articleRepository.findAll();
    }

    public List<Article> getAllArticles(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        logger.info("pageable" + pageable);
        return articleRepository.findAllArticlesByPage(pageable, redisTemplate, objectMapper);
    }

    public Article getArticleById(String id) {
        Optional<Article> optionalArticle = articleRepository.findById(Integer.parseInt(id.trim()));
        return optionalArticle.orElse(null);
    }

    public List<Article> findArticlesByKeyword(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return articleRepository.findByTitleOrContentContaining(keyword, pageable);
    }


    public List<Article> findArticlesByTopics(int page, Category favoriteTopic, Category recommendTopic1, Category recommendTopic2, int pageSize) {

        List<Article> articles = new ArrayList<>();
        logger.info("pageSize" + pageSize);
        // 計算每一類文章的數量
        int favoriteTopicCount = (int) Math.ceil(7.0 / (7 + 2 + 1) * pageSize);
        int recommendTopic1Count = (int) Math.ceil(2.0 / (7 + 2 + 1) * pageSize);
        int recommendTopic2Count = (int) Math.ceil(1.0 / (7 + 2 + 1) * pageSize);


        // 確保每一類文章的數量不超過pageSize
        favoriteTopicCount = Math.min(favoriteTopicCount, pageSize);
        recommendTopic1Count = Math.min(recommendTopic1Count, pageSize - favoriteTopicCount);
        recommendTopic2Count = Math.min(recommendTopic2Count, pageSize - favoriteTopicCount - recommendTopic1Count);
//        logger.info("favoriteTopicCount: " + favoriteTopicCount);
//        logger.info("recommendTopic1Count: " + recommendTopic1Count);
//        logger.info("recommendTopic2Count: " + recommendTopic2Count);


        // 根據每一類文章的數量查找相應的文章
        Pageable pageable = PageRequest.of(page, pageSize);
//        logger.info("pageable: " + pageable);
//        logger.info("Page: " + page);
//        logger.info("pageSize: " + pageSize);


        List<Article> favoriteTopicArticles = articleRepository.findFavoriteTopicArticles(pageable, favoriteTopic);
        List<Article> recommendTopic1Articles = articleRepository.findRecommendTopic1Articles(pageable, recommendTopic1);
        List<Article> recommendTopic2Articles = articleRepository.findRecommendTopic2Articles(pageable, recommendTopic2);

//        logger.info("favoriteTopicArticles:" + favoriteTopicArticles);
//        logger.info("recommendTopic1Articles:" + recommendTopic1Articles);
//        logger.info("recommendTopic2Articles:" + recommendTopic2Articles);
//
//        logger.info("favoriteTopic:" + favoriteTopic);
//        logger.info("recommendTopic1:" + recommendTopic1);
//        logger.info("recommendTopic2:" + recommendTopic2);

        articles.addAll(favoriteTopicArticles.subList(0, Math.min(favoriteTopicArticles.size(), favoriteTopicCount)));
        articles.addAll(recommendTopic1Articles.subList(0, Math.min(recommendTopic1Articles.size(), recommendTopic1Count)));
        articles.addAll(recommendTopic2Articles.subList(0, Math.min(recommendTopic2Articles.size(), recommendTopic2Count)));


        // 將文章列表打亂
        Collections.shuffle(articles);

        return articles;
//        return articleRepository.findAllArticlesByPageAndTopics(favoriteTopic, recommendTopic1, recommendTopic2, pageable);
    }


    public void postClickEvent(UserClickEvent userClickEvent) {
        userClickEventRepository.save(userClickEvent);
    }

    public void calculateCosineSimilarity() {
        List<UserClickDetail> userClickDetails = userClickDetailRepository.findAll();

        // 每個話題的點擊次數
        double cnClickLength = 0.0;
        double bmClickLength = 0.0;
        double cdClickLength = 0.0;
        double arClickLength = 0.0;
        double otClickLength = 0.0;


        for (UserClickDetail userClickDetail : userClickDetails) {
            cnClickLength += Math.pow(userClickDetail.getCompanyNewsClick(), 2);
            bmClickLength += Math.pow(userClickDetail.getBroadMarketNewsClick(), 2);
            cdClickLength += Math.pow(userClickDetail.getCompanyDiscussionClick(), 2);
            arClickLength += Math.pow(userClickDetail.getAdviceRequestClick(), 2);
            otClickLength += Math.pow(userClickDetail.getOthersClick(), 2);
        }

        cnClickLength = Math.sqrt(cnClickLength);
        bmClickLength = Math.sqrt(bmClickLength);
        cdClickLength = Math.sqrt(cdClickLength);
        arClickLength = Math.sqrt(arClickLength);
        otClickLength = Math.sqrt(otClickLength);

        // 話題間的內積
        double cnBmDotProduct = 0.0;
        double cnCdDotProduct = 0.0;
        double cnArDotProduct = 0.0;
        double cnOtDotProduct = 0.0;

        double bmCdDotProduct = 0.0;
        double bmArDotProduct = 0.0;
        double bmOtDotProduct = 0.0;

        double cdOtDotProduct = 0.0;
        double cdArDotProduct = 0.0;
        double arOtDotProduct = 0.0;


        for (UserClickDetail userClickDetail : userClickDetails) {
            cnBmDotProduct += userClickDetail.getCompanyNewsClick() * userClickDetail.getBroadMarketNewsClick();
            cnCdDotProduct += userClickDetail.getCompanyNewsClick() * userClickDetail.getCompanyDiscussionClick();
            cnArDotProduct += userClickDetail.getCompanyNewsClick() * userClickDetail.getAdviceRequestClick();
            cnOtDotProduct += userClickDetail.getCompanyNewsClick() * userClickDetail.getOthersClick();

            bmCdDotProduct += userClickDetail.getBroadMarketNewsClick() * userClickDetail.getCompanyDiscussionClick();
            bmArDotProduct += userClickDetail.getBroadMarketNewsClick() * userClickDetail.getAdviceRequestClick();
            bmOtDotProduct += userClickDetail.getBroadMarketNewsClick() * userClickDetail.getOthersClick();

            cdOtDotProduct += userClickDetail.getCompanyDiscussionClick() * userClickDetail.getOthersClick();
            cdArDotProduct += userClickDetail.getCompanyDiscussionClick() * userClickDetail.getAdviceRequestClick();
            arOtDotProduct += userClickDetail.getAdviceRequestClick() * userClickDetail.getOthersClick();
        }

        // 計算cosine similarity
        double cnBmSimilarity = cnBmDotProduct / (cnClickLength * bmClickLength);
        double cnCdSimilarity = cnCdDotProduct / (cnClickLength * cdClickLength);
        double cnArSimilarity = cnArDotProduct / (cnClickLength * arClickLength);
        double cnOtSimilarity = cnOtDotProduct / (cnClickLength * otClickLength);

        double bmCdSimilarity = bmCdDotProduct / (bmClickLength * cdClickLength);
        double bmArSimilarity = bmArDotProduct / (bmClickLength * arClickLength);
        double bmOtSimilarity = bmOtDotProduct / (bmClickLength * otClickLength);

        double cdOtSimilarity = cdOtDotProduct / (cdClickLength * otClickLength);
        double cdArSimilarity = cdArDotProduct / (cdClickLength * arClickLength);
        double arOtSimilarity = arOtDotProduct / (arClickLength * otClickLength);

        // 更新recommend topics
        for (UserClickDetail userClickDetail : userClickDetails) {

            //使用外鍵的值
            Category favoriteTopic = userClickDetail.getFavoriteTopic();
            String favoriteTopicName = favoriteTopic.getCategory();

            Category recommendTopic1;
            Category recommendTopic2;

            // 根據favorite_topic決定要比對哪個餘弦相似度
            switch (favoriteTopicName) {

                case "Company News":
                    double[] similaritiesCN = {cnBmSimilarity, cnCdSimilarity, cnArSimilarity, cnOtSimilarity};
                    String[] topicsCN = {"Broad market news", "Company Discussion", "Advice Request", "Others"};

                    int maxIndexCN = 0;
                    for (int i = 1; i < similaritiesCN.length; i++) {
                        if (similaritiesCN[i] > similaritiesCN[maxIndexCN]) {
                            maxIndexCN = i;
                        }
                    }

                    recommendTopic1 = categoryRepository.findByCategory(topicsCN[maxIndexCN]);

                    // 移除最大值(設成一個非常小的值)，找到次大值
                    similaritiesCN[maxIndexCN] = Double.MIN_VALUE;

                    // 再次找到最大值 -> 次高相似度
                    maxIndexCN = 0;
                    for (int i = 1; i < similaritiesCN.length; i++) {
                        if (similaritiesCN[i] > similaritiesCN[maxIndexCN]) {
                            maxIndexCN = i;
                        }
                    }

                    // 次高相似度是推薦主題2
                    recommendTopic2 = categoryRepository.findByCategory(topicsCN[maxIndexCN]);

                    break;

                case "Broad market news":
                    double[] similaritiesBM = {cnBmSimilarity, bmCdSimilarity, bmArSimilarity, bmOtSimilarity};
                    String[] topicsBM = {"Company News", "Company Discussion", "Advice Request", "Others"};

                    int maxIndexBM = 0;
                    for (int i = 1; i < similaritiesBM.length; i++) {
                        if (similaritiesBM[i] > similaritiesBM[maxIndexBM]) {
                            maxIndexBM = i;
                        }
                    }

                    recommendTopic1 = categoryRepository.findByCategory(topicsBM[maxIndexBM]);

                    // 移除最大值(設成一個非常小的值)，找到次大值
                    similaritiesBM[maxIndexBM] = Double.MIN_VALUE;

                    // 再次找到最大值 -> 次高相似度
                    maxIndexBM = 0;
                    for (int i = 1; i < similaritiesBM.length; i++) {
                        if (similaritiesBM[i] > similaritiesBM[maxIndexBM]) {
                            maxIndexBM = i;
                        }
                    }

                    // 次高相似度是推薦主題2
                    recommendTopic2 = categoryRepository.findByCategory(topicsBM[maxIndexBM]);
                    break;

                case "Company Discussion":
                    double[] similaritiesCD = {cnCdSimilarity, bmCdSimilarity, cdOtSimilarity, cdArSimilarity};
                    String[] topicsCD = {"Company News", "Broad market news", "Others", "Advice Request"};

                    int maxIndexCD = 0;
                    for (int i = 1; i < similaritiesCD.length; i++) {
                        if (similaritiesCD[i] > similaritiesCD[maxIndexCD]) {
                            maxIndexCD = i;
                        }
                    }

                    recommendTopic1 = categoryRepository.findByCategory(topicsCD[maxIndexCD]);

                    // 移除最大值(設成一個非常小的值)，找到次大值
                    similaritiesCD[maxIndexCD] = Double.MIN_VALUE;

                    // 再次找到最大值 -> 次高相似度
                    maxIndexCD = 0;
                    for (int i = 1; i < similaritiesCD.length; i++) {
                        if (similaritiesCD[i] > similaritiesCD[maxIndexCD]) {
                            maxIndexCD = i;
                        }
                    }

                    // 次高相似度是推薦主題2
                    recommendTopic2 = categoryRepository.findByCategory(topicsCD[maxIndexCD]);
                    break;

                case "Advice Request":
                    double[] similaritiesAR = {cnArSimilarity, bmArSimilarity, cdArSimilarity, arOtSimilarity};
                    String[] topicsAR = {"Company News", "Broad market news", "Company Discussion", "Others"};

                    int maxIndexAR = 0;
                    for (int i = 1; i < similaritiesAR.length; i++) {
                        if (similaritiesAR[i] > similaritiesAR[maxIndexAR]) {
                            maxIndexAR = i;
                        }
                    }

                    recommendTopic1 = categoryRepository.findByCategory(topicsAR[maxIndexAR]);

                    // 移除最大值(設成一個非常小的值)，找到次大值
                    similaritiesAR[maxIndexAR] = Double.MIN_VALUE;

                    // 再次找到最大值 -> 次高相似度
                    maxIndexAR = 0;
                    for (int i = 1; i < similaritiesAR.length; i++) {
                        if (similaritiesAR[i] > similaritiesAR[maxIndexAR]) {
                            maxIndexCD = i;
                        }
                    }

                    // 次高相似度是推薦主題2
                    recommendTopic2 = categoryRepository.findByCategory(topicsAR[maxIndexAR]);
                    break;


                case "Others":
                    double[] similaritiesOT = {cnOtSimilarity, bmOtSimilarity, cdOtSimilarity, arOtSimilarity};
                    String[] topicsOT = {"Company News", "Broad market news", "Company Discussion", "Advice Request"};

                    int maxIndexOT = 0;
                    for (int i = 1; i < similaritiesOT.length; i++) {
                        if (similaritiesOT[i] > similaritiesOT[maxIndexOT]) {
                            maxIndexOT = i;
                        }
                    }

                    recommendTopic1 = categoryRepository.findByCategory(topicsOT[maxIndexOT]);

                    // 移除最大值(設成一個非常小的值)，找到次大值
                    similaritiesOT[maxIndexOT] = Double.MIN_VALUE;

                    // 再次找到最大值 -> 次高相似度
                    maxIndexOT = 0;
                    for (int i = 1; i < similaritiesOT.length; i++) {
                        if (similaritiesOT[i] > similaritiesOT[maxIndexOT]) {
                            maxIndexCD = i;
                        }
                    }

                    // 次高相似度是推薦主題2
                    recommendTopic2 = categoryRepository.findByCategory(topicsOT[maxIndexOT]);
                    break;

                default:
                    recommendTopic1 = null;
                    recommendTopic2 = null;
            }

            userClickDetail.setRecommendTopic1(recommendTopic1);
            userClickDetail.setRecommendTopic2(recommendTopic2);
            userClickDetailRepository.save(userClickDetail);
        }
    }

    public void updateUserClickDetail() {
        List<Object[]> results = userClickEventRepository.countClicksByUserId();
        for (Object[] result : results) {
            Integer userId = (Integer) result[0]; // Convert Object to Integer directly
            Integer companyNewsClick = ((BigDecimal) result[1]).intValue(); // Convert BigDecimal to Integer
            Integer broadMarketNewsClick = ((BigDecimal) result[2]).intValue(); // Convert BigDecimal to Integer
            Integer companyDiscussionClick = ((BigDecimal) result[3]).intValue(); // Convert BigDecimal to Integer
            Integer adviceRequestClick = ((BigDecimal) result[4]).intValue(); // Convert BigDecimal to Integer
            Integer othersClick = ((BigDecimal) result[5]).intValue(); // Convert BigDecimal to Integer

            String[] topics = {"Company News", "Broad market news", "Company Discussion", "Advice Request", "Others"};
            Integer[] clicks = {companyNewsClick, broadMarketNewsClick, companyDiscussionClick, adviceRequestClick, othersClick};

            // 找到點擊次數最多的類別
            int maxIndex = 0;
            for (int i = 1; i < clicks.length; i++) {
                if (clicks[i] > clicks[maxIndex]) {
                    maxIndex = i;
                }
            }

            // 將最多點擊次數對應的類別作為favorite topic
//                String favoriteTopic = topics[maxIndex];

            // 儲存外鍵的值
            Category favoriteTopic = categoryRepository.findByCategory(topics[maxIndex]);

//                String favoriteTopic = "";
            String recommendTopic1 = ""; // define how to get recommend topic 1
            String recommendTopic2 = ""; // define how to get recommend topic 2

            UserClickDetail detail = userClickDetailRepository.findByUserId(userId)
                    .orElse(new UserClickDetail());
            detail.setUserId(userId);
            detail.setCompanyNewsClick(companyNewsClick);
            detail.setBroadMarketNewsClick(broadMarketNewsClick);
            detail.setCompanyDiscussionClick(companyDiscussionClick);
            detail.setAdviceRequestClick(adviceRequestClick);
            detail.setOthersClick(othersClick);
            detail.setFavoriteTopic(favoriteTopic);
//                detail.setRecommendTopic1(recommendTopic1);
//                detail.setRecommendTopic2(recommendTopic2);
            userClickDetailRepository.save(detail);
        }
    }


}

