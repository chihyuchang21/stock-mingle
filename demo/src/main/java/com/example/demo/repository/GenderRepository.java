package com.example.demo.repository;

import com.example.demo.model.user.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenderRepository extends JpaRepository<Gender, Integer> {

    // 根據性別值查詢 Gender Object
    Gender findByGender(String gender);
}
