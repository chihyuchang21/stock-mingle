package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.ArticleComment;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
    @Autowired
    private ArticleCommentRepository articleCommentRepository;


    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public void postArticle(Article article) {
        articleRepository.save(article);
        redisTemplate.delete("articles");
    }

    public void postComment(ArticleComment articleComment) {
        articleCommentRepository.save(articleComment);
        updateArticleCommentCount(articleComment.getArticleId());
    }

    public void updateArticleCommentCount(String articleId) {
        String commentCount = articleCommentRepository.countByArticleId(articleId);
        int commentCountInt = Integer.parseInt(commentCount);
        int articleIdInt = Integer.parseInt(articleId);
        Article article = articleRepository.findById(articleIdInt).orElse(null);
        article.setCommentCount(commentCountInt);
        articleRepository.save(article);
    }

    public List<Article> getAllArticle() {
        return articleRepository.findAll();
    }

    public List<Article> getAllArticles(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return articleRepository.findAllArticlesByPage(pageable, redisTemplate, objectMapper);
    }

    public Article getArticleById(String id) {
        Optional<Article> optionalArticle = articleRepository.findById(Integer.parseInt(id.trim()));
        return optionalArticle.orElse(null);
    }

    public List<ArticleComment> getCommentByArticleId(String id) {
        List<ArticleComment> comments = articleCommentRepository.findByArticleId(id);
        return comments;
    }


    public List<Article> findArticlesByKeyword(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return articleRepository.findByTitleOrContentContaining(keyword, pageable);
    }

    public Article toggleLike(Integer articleId) {
        // 使用 findById 方法獲取 Optional<Article> 對象 (JPA內建為Optional)
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        // 檢查是否存在對應的文章
        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get(); // 從 Optional 中取得 Article 對象
            // 根據 isLiked 參數來新增或減少點讚數量
            article.setLikeCount(article.getLikeCount() + 1);
            // 更新文章到資料庫
            return articleRepository.save(article);
        } else {
            throw new IllegalArgumentException("Article not found with ID: " + articleId);
        }
    }

    public Article cancelLike(Integer articleId) {
        // 使用 findById 方法獲取 Optional<Article> 對象 (JPA內建為Optional)
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        // 檢查是否存在對應的文章
        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get(); // 從 Optional 中取得 Article 對象
            // 根據 isLiked 參數來新增或減少點讚數量
            article.setLikeCount(article.getLikeCount() - 1);
            // 更新文章到資料庫
            return articleRepository.save(article);
        } else {
            throw new IllegalArgumentException("Article not found with ID: " + articleId);
        }
    }


    public int countTotalArticles() {
        return articleRepository.countTotalArticles();
    }


    public List<Article> findArticlesByTopics(int page, Category favoriteTopic, Category recommendTopic1, Category recommendTopic2, int pageSize) {

        List<Article> articles = new ArrayList<>();
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
//        Collections.shuffle(articles);

        return articles;
//        return articleRepository.findAllArticlesByPageAndTopics(favoriteTopic, recommendTopic1, recommendTopic2, pageable);
    }


    public void postClickEvent(UserClickEvent userClickEvent) {
        userClickEventRepository.save(userClickEvent);
    }

    @Scheduled(cron = "0 15 23 * * ?") // 每天23:15執行
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

                    int maxIndexCN = 0; // TD-DO: using similarities sorting
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

    //    ========================== Function Refactor ===============================    //
    public void calculateCosineSimilarityRefactor() {
        List<UserClickDetail> userClickDetails = userClickDetailRepository.findAll();

        // Calculate topic click lengths
        Map<String, Double> topicClickLengths = calculateTopicClickLengths(userClickDetails);

        // Calculate topic dots products
        Map<String, Map<String, Double>> topicDotProducts = calculateTopicDotProducts(userClickDetails);

        // Calculate cosine similarity
        Map<String, Map<String, Double>> cosineSimilarities = calculateCosineSimilarities(topicClickLengths, topicDotProducts);

        // update recommend topics1```````````````````````
        updateRecommendTopics(userClickDetails, cosineSimilarities);
    }

    private Map<String, Double> calculateTopicClickLengths(List<UserClickDetail> userClickDetails) {
        Map<String, Double> topicClickLengths = new HashMap<>();
        for (UserClickDetail userClickDetail : userClickDetails) {
            double cnClickLength = Math.pow(userClickDetail.getCompanyNewsClick(), 2);
            double bmClickLength = Math.pow(userClickDetail.getBroadMarketNewsClick(), 2);
            double cdClickLength = Math.pow(userClickDetail.getCompanyDiscussionClick(), 2);
            double arClickLength = Math.pow(userClickDetail.getAdviceRequestClick(), 2);
            double otClickLength = Math.pow(userClickDetail.getOthersClick(), 2);
            // To-do: space
            // HashMap .getOrDefault https://www.runoob.com/java/java-hashmap-getordefault.html
            topicClickLengths.put("Company News", topicClickLengths.getOrDefault("Company News", 0.0) + cnClickLength);
            topicClickLengths.put("Broad market news", topicClickLengths.getOrDefault("Broad market news", 0.0) + bmClickLength);
            topicClickLengths.put("Company Discussion", topicClickLengths.getOrDefault("Company Discussion", 0.0) + cdClickLength);
            topicClickLengths.put("Advice Request", topicClickLengths.getOrDefault("Advice Request", 0.0) + arClickLength);
            topicClickLengths.put("Others", topicClickLengths.getOrDefault("Others", 0.0) + otClickLength);
        }
        // Using .replaceAll to do Math.sqrt
        // HashMap .replaceAll method https://www.runoob.com/java/java-hashmap-replaceall.html
        topicClickLengths.replaceAll((k, v) -> Math.sqrt(v));
        return topicClickLengths;
    }

    private Map<String, Map<String, Double>> calculateTopicDotProducts(List<UserClickDetail> userClickDetails) {
        Map<String, Map<String, Double>> topicDotProducts = new HashMap<>();
        for (UserClickDetail userClickDetail : userClickDetails) {
            double cnClick = userClickDetail.getCompanyNewsClick();
            double bmClick = userClickDetail.getBroadMarketNewsClick();
            double cdClick = userClickDetail.getCompanyDiscussionClick();
            double arClick = userClickDetail.getAdviceRequestClick();
            double otClick = userClickDetail.getOthersClick();

            // Calculating Dot Product
            Map<String, Double> dotProducts = topicDotProducts.computeIfAbsent("Company News", k -> new HashMap<>());
            dotProducts.put("Broad market news", dotProducts.getOrDefault("Broad market news", 0.0) + cnClick * bmClick);
            dotProducts.put("Company Discussion", dotProducts.getOrDefault("Company Discussion", 0.0) + cnClick * cdClick);
            dotProducts.put("Advice Request", dotProducts.getOrDefault("Advice Request", 0.0) + cnClick * arClick);
            dotProducts.put("Others", dotProducts.getOrDefault("Others", 0.0) + cnClick * otClick);

            // Repeat the above process for other topics

            // Update dot products for other topics
        }
        return topicDotProducts;
    }

    // Map -> EntrySet (for each key value)
    private Map<String, Map<String, Double>> calculateCosineSimilarities(Map<String, Double> topicClickLengths, Map<String, Map<String, Double>> topicDotProducts) {
        Map<String, Map<String, Double>> cosineSimilarities = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : topicDotProducts.entrySet()) {
            String topic = entry.getKey();
            Map<String, Double> dotProducts = entry.getValue();
            Map<String, Double> similarities = new HashMap<>();
            for (Map.Entry<String, Double> dotProductEntry : dotProducts.entrySet()) {
                String otherTopic = dotProductEntry.getKey();
                double dotProduct = dotProductEntry.getValue();
                double similarity = dotProduct / (topicClickLengths.get(topic) * topicClickLengths.get(otherTopic));
                similarities.put(otherTopic, similarity);
            }
            cosineSimilarities.put(topic, similarities);
        }
        return cosineSimilarities;
    }

    private void updateRecommendTopics(List<UserClickDetail> userClickDetails, Map<String, Map<String, Double>> cosineSimilarities) {
        for (UserClickDetail userClickDetail : userClickDetails) {
            Category favoriteTopic = userClickDetail.getFavoriteTopic();
            String favoriteTopicName = favoriteTopic.getCategory();
            Map<String, Double> similarities = cosineSimilarities.get(favoriteTopicName);

//            (entry, key, value)
            // Find the first and second most similar topics
            String recommendTopic1 = similarities.entrySet().stream()
                    .max(Map.Entry.comparingByValue()) // sorting by value?
                    .map(Map.Entry::getKey)
                    .orElse(null); // why?

            similarities.remove(recommendTopic1); // Remove the first most similar topic

            String recommendTopic2 = similarities.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            // Update the userClickDetail with recommended topics
            userClickDetail.setRecommendTopic1(categoryRepository.findByCategory(recommendTopic1));
            userClickDetail.setRecommendTopic2(categoryRepository.findByCategory(recommendTopic2));
            userClickDetailRepository.save(userClickDetail);
        }
    }
    //    ========================== Function Refactor ===============================    //


    @Scheduled(cron = "0 0 23 * * ?") // 每天23:00執行
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

