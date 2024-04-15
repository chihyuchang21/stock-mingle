package com.example.demo.repository;

import com.example.demo.model.user.UserClickDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserClickDetailRepository extends JpaRepository<UserClickDetail, Integer> {
}
