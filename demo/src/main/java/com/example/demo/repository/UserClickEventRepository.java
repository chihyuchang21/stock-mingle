package com.example.demo.repository;

import com.example.demo.model.user.UserClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserClickEventRepository extends JpaRepository<UserClickEvent, Integer> {
    @Query(value = "SELECT user_id, " +
            "SUM(IF(category_id = 1, 1, 0)) AS company_news_click, " +
            "SUM(IF(category_id = 2, 1, 0)) AS broad_market_news_click, " +
            "SUM(IF(category_id = 3, 1, 0)) AS company_discussion_click, " +
            "SUM(IF(category_id = 4, 1, 0)) AS advice_request_click, " +
            "SUM(IF(category_id = 5, 1, 0)) AS others_click " +
            "FROM user_click_event " +
            "GROUP BY user_id", nativeQuery = true)
    List<Object[]> countClicksByUserId();
}

