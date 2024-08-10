package com.example.demo.repository;

import com.example.demo.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Logger logger = LoggerFactory.getLogger(UserRepository.class);

    List<User> findAll();

    boolean existsByAccountName(String accountName);

    User getUserByAccountName(String accountName);

    // Query the user ID based on the account name
    @Query("SELECT u.id FROM User u WHERE u.accountName = :accountName")
    Integer getUserIdByAccountName(@Param("accountName") String accountName);

    // Inform Spring Data JPA that this is an update operation
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.image = :fileUrlSaved WHERE u.id = :userId")
    void updateUserImage(@Param("fileUrlSaved") String fileUrlSaved, @Param("userId") Integer userId);

}

