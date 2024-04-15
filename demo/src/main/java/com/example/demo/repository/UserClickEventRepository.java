package com.example.demo.repository;

import com.example.demo.model.user.UserClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserClickEventRepository extends JpaRepository<UserClickEvent, Integer> {
}
