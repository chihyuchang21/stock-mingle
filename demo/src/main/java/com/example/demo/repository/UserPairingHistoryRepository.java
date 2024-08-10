package com.example.demo.repository;

import com.example.demo.model.user.UserPairingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface UserPairingHistoryRepository extends JpaRepository<UserPairingHistory, Long> {

    Logger logger = LoggerFactory.getLogger(UserRepository.class);

    boolean existsByUser1IdAndUser2Id(Integer user1Id, Integer user2Id);

    @Query(value = "SELECT * FROM user_pairing_history " +
            "WHERE (user1_id = :userId OR user2_id = :userId) AND timestamp = :timestamp", nativeQuery = true)
    List<UserPairingHistory> findByUserIdAndTimestamp(Integer userId, Timestamp timestamp);

    @Query("SELECT CASE " +
            "WHEN u.user1Id = :id THEN u.user2Id " +
            "WHEN u.user2Id = :id THEN u.user1Id " +
            "END " +
            "FROM UserPairingHistory u " +
            "WHERE u.user1Id = :id OR u.user2Id = :id")
    List<UserPairingHistory> findOtherUserId(@Param("id") Integer id);

}
