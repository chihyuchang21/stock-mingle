package com.example.demo.repository;

import com.example.demo.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Logger logger = LoggerFactory.getLogger(UserRepository.class);

    List<User> findAll();

    boolean existsByAccountName(String accountName);

    User getUserByAccountName(String accountName);

    // 根據帳號名查詢用戶 ID
    @Query("SELECT u.id FROM User u WHERE u.accountName = :accountName")
    Integer getUserIdByAccountName(String accountName);

}

