package com.example.demo.repository;

import com.example.demo.model.user.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Integer> {
    // 以hashtag名稱查詢對應的Hashtag對象
    Optional<Hashtag> findByHashtagName(String hashtagName);
}

