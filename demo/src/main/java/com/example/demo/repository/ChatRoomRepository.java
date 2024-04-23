package com.example.demo.repository;

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
}
