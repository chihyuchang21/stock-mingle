package com.example.demo.repository;

import com.example.demo.model.user.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenderRepository extends JpaRepository<Gender, Integer> {

    // Query the Gender object based on the gender value
    Gender findByGender(String gender);
}
