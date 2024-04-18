package com.example.demo.repository;

import com.example.demo.model.user.User;
import com.example.demo.model.user.UserPairingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Logger logger = LoggerFactory.getLogger(UserRepository.class);

    List<User> findAll();

    boolean existsByAccountName(String accountName);

    User getUserByAccountName(String accountName);

    @Query(value = "SELECT * FROM user_pairing_history " +
            "WHERE (user1_id = :userId OR user2_id = :userId) AND timestamp = :timestamp", nativeQuery = true)
    List<UserPairingHistory> findByUserIdAndTimestamp(Integer userId, Timestamp timestamp);


    // 根據帳號名查詢用戶 ID
    @Query("SELECT u.id FROM User u WHERE u.accountName = :accountName")
    Integer getUserIdByAccountName(String accountName);

    default void logQueryParameters(Integer userId, Timestamp timestamp) {
        logger.info("Querying user_pairing_history with userId: {} and timestamp: {}", userId, timestamp);
    }


}

