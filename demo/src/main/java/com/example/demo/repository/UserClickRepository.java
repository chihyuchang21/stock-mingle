package com.example.demo.repository;

import com.example.demo.model.stock.StockInformation;
import com.example.demo.model.user.UserClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserClickRepository extends JpaRepository<UserClick, Long> {
}
