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


//    @Query(value = "SELECT * FROM user_pairing_history " +
//            "JOIN User u1 ON user_pairing_history.user1_id = u1.id " +
//            "JOIN User u2 ON user_pairing_history.user2_id = u2.id " +
//            "WHERE (user_pairing_history.user1_id = :userId OR user_pairing_history.user2_id = :userId) AND user_pairing_history.timestamp = :timestamp", nativeQuery = true)
//    List<UserPairingHistory> findByUserIdAndTimestamp(Integer userId, Timestamp timestamp);
//
//    @Query(value = "SELECT CASE WHEN user_pairing_history.user1.id = :userId THEN u2.nickname ELSE u1.nickname END, " +
//            "CASE WHEN user_pairing_history.user1_id = :userId THEN u2.image ELSE u1.image END " +
//            "FROM UserPairingHistory user_pairing_history " +
//            "JOIN User u1 ON user_pairing_history.user1_id = u1.id " +
//            "JOIN User u2 ON user_pairing_history.user2_id = u2.id " +
//            "WHERE (user_pairing_history.user1_id = :userId OR user_pairing_history.user2.id = :userId) AND user_pairing_history.timestamp = :timestamp", nativeQuery = true)
//    List<Object[]> findNicknameAndImageByUserIdAndTimestamp(Integer userId, Timestamp timestamp);

    default void logQueryParameters(Integer userId, Timestamp timestamp) {
        logger.info("Querying user_pairing_history with userId: {} and timestamp: {}", userId, timestamp);
    }
}
