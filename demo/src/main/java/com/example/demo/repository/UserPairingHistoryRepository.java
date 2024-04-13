package com.example.demo.repository;

import com.example.demo.model.user.UserHashtag;
import com.example.demo.model.user.UserPairingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPairingHistoryRepository extends JpaRepository<UserPairingHistory, Long> {
    boolean existsByUser1IdAndUser2Id(Integer user1Id, Integer user2Id);
}
