package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.model.user.UserClick;
import com.example.demo.model.user.UserClickDetail;
import com.example.demo.model.user.UserClickEvent;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.UserClickDetailRepository;
import com.example.demo.repository.UserClickEventRepository;
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

    @Autowired
    private UserClickEventRepository userClickEventRepository;

    @Autowired
    private UserClickDetailRepository userClickDetailRepository;

    public void postArticle(Article article){
        articleRepository.save(article);
    }

    public List<Article> getAllArticle(){
        return articleRepository.findAll();
    }

    public void postClickEvent(UserClickEvent userClickEvent) { userClickEventRepository.save(userClickEvent);}

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
        String favoriteTopic = userClickDetail.getFavoriteTopic();
        String recommendTopic1;
        String recommendTopic2;

        // 根據favorite_topic決定要比對哪個餘弦相似度
        switch (favoriteTopic) {

            case "cn":
                double[] similaritiesCN = {cnBmSimilarity, cnCdSimilarity, cnArSimilarity, cnOtSimilarity};
                String[] topicsCN = {"bm", "cd", "ar", "ot"};

                int maxIndexCN = 0;
                for (int i = 1; i < similaritiesCN.length; i++) {
                    if (similaritiesCN[i] > similaritiesCN[maxIndexCN]) {
                        maxIndexCN = i;
                    }
                }

                recommendTopic1 = topicsCN[maxIndexCN];

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
                recommendTopic2 = topicsCN[maxIndexCN];

                break;

            case "bm":
                double[] similaritiesBM = {cnBmSimilarity, bmCdSimilarity, bmArSimilarity, bmOtSimilarity};
                String[] topicsBM = {"cn", "cd", "ar", "ot"};

                int maxIndexBM = 0;
                for (int i = 1; i < similaritiesBM.length; i++) {
                    if (similaritiesBM[i] > similaritiesBM[maxIndexBM]) {
                        maxIndexBM = i;
                    }
                }

                recommendTopic1 = topicsBM[maxIndexBM];

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
                recommendTopic2 = topicsBM[maxIndexBM];
                break;

            case "cd":
                double[] similaritiesCD = {cnCdSimilarity, bmCdSimilarity, cdOtSimilarity, cdArSimilarity};
                String[] topicsCD = {"cn", "bm", "ot", "ar"};

                int maxIndexCD = 0;
                for (int i = 1; i < similaritiesCD.length; i++) {
                    if (similaritiesCD[i] > similaritiesCD[maxIndexCD]) {
                        maxIndexCD = i;
                    }
                }

                recommendTopic1 = topicsCD[maxIndexCD];

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
                recommendTopic2 = topicsCD[maxIndexCD];
                break;

                case "ar":
                    double[] similaritiesAR = {cnArSimilarity, bmArSimilarity, cdArSimilarity, arOtSimilarity};
                    String[] topicsAR = {"cn", "bm", "cd", "ot"};

                    int maxIndexAR = 0;
                    for (int i = 1; i < similaritiesAR.length; i++) {
                        if (similaritiesAR[i] > similaritiesAR[maxIndexAR]) {
                            maxIndexAR = i;
                        }
                    }

                    recommendTopic1 = topicsAR[maxIndexAR];

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
                    recommendTopic2 = topicsAR[maxIndexAR];
                break;


                case "ot":
                    double[] similaritiesOT = {cnOtSimilarity, bmOtSimilarity, cdOtSimilarity, arOtSimilarity};
                    String[] topicsOT = {"cn", "bm", "cd", "ar"};

                    int maxIndexOT = 0;
                    for (int i = 1; i < similaritiesOT.length; i++) {
                        if (similaritiesOT[i] > similaritiesOT[maxIndexOT]) {
                            maxIndexOT = i;
                        }
                    }

                    recommendTopic1 = topicsOT[maxIndexOT];

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
                    recommendTopic2 = topicsOT[maxIndexOT];
                break;

            default:
                recommendTopic1 = "";
                recommendTopic2 = "";
        }

        userClickDetail.setRecommendTopic1(recommendTopic1);
        userClickDetail.setRecommendTopic2(recommendTopic2);
        userClickDetailRepository.save(userClickDetail);
        }
    }
}
