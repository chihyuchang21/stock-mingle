package com.example.demo.repository;

import com.example.demo.model.user.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Integer> {
    // Query the corresponding Hashtag object by the hashtag name.
    Optional<Hashtag> findByHashtagName(String hashtagName);
}

