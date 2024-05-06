package com.example.demo.repository;

import com.example.demo.dto.ChatRoomInfo;
import com.example.demo.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<Message, Long> {
    List<Message> findByUserPairingHistoryId(Integer userPairingHistoryId);

    @Query("SELECT DISTINCT m.userPairingHistoryId FROM Message m WHERE m.senderUserId = :userId")
    List<Integer> findUserPairingHistoryIdsByUserId(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT NEW com.example.demo.dto.ChatRoomInfo(m.userPairingHistoryId, " +
            "CASE WHEN m.senderUserId = up.user1Id THEN up.user2Id " +
            "WHEN m.senderUserId = up.user2Id THEN up.user1Id ELSE NULL END, u.nickname) " +
            "FROM Message m " +
            "JOIN UserPairingHistory up ON m.userPairingHistoryId = up.id " +
            "JOIN User u ON CASE WHEN m.senderUserId = up.user1Id THEN up.user2Id " +
            "WHEN m.senderUserId = up.user2Id THEN up.user1Id ELSE NULL END = u.id " +
            "WHERE m.senderUserId = :userId")
    List<ChatRoomInfo> findChatroomInfoByUserId(@Param("userId") Integer userId);
}
