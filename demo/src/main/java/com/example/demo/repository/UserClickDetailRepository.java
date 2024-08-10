package com.example.demo.repository;

import com.example.demo.model.user.UserClickDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserClickDetailRepository extends JpaRepository<UserClickDetail, Integer> {
    // Use Optional to store potentially null values
    Optional<UserClickDetail> findByUserId(Integer userId);
}
