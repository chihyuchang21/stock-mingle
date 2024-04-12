package com.example.demo.repository;

import com.example.demo.model.user.UserHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHashtagRepository extends JpaRepository<UserHashtag, Integer> {
    UserHashtag findByUserId(Integer userId);
}
