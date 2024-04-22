package com.example.demo.repository;

import com.example.demo.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<Message, Long> {
    List<Message> findByUserPairingHistoryId(Integer userPairingHistoryId);
}
