package com.example.demo.repository;

import com.example.demo.model.user.UserHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHashtagRepository extends JpaRepository<UserHashtag, Integer> {
    List<UserHashtag> findByUserId(Integer userId);

}
