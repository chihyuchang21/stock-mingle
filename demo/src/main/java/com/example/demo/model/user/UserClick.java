package com.example.demo.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer usClick;
    private Integer twClick;
    private Integer dtClick;
    private String favoriteTopic;
    private String recommendTopic1;
    private String recommendTopic2;

    // 計算最受歡迎的主題並返回
//    public String getFavoriteTopic() {
//        if (companyNewsClick >= broadMarketNewsClick && companyNewsClick >= companyDiscussionClick && companyNewsClick >= adviceRequestClick && companyNewsClick >= othersClick) {
//            return "company_news";
//        } else if (broadMarketNewsClick >= companyNewsClick && broadMarketNewsClick >= companyDiscussionClick && broadMarketNewsClick >= adviceRequestClick && broadMarketNewsClick >= othersClick) {
//            return "broad_market_news";
//        } else if (companyDiscussionClick >= companyNewsClick && companyDiscussionClick >= broadMarketNewsClick && companyDiscussionClick >= adviceRequestClick && companyDiscussionClick >= othersClick) {
//            return "company_discussion";
//        } else if (adviceRequestClick >= companyNewsClick && adviceRequestClick >= broadMarketNewsClick && adviceRequestClick >= companyDiscussionClick && adviceRequestClick >= othersClick) {
//            return "advice_request";
//        } else {
//            return "others";
//        }
//    }

}
