package com.example.demo.repository;

import com.example.demo.model.user.UserClickDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserClickDetailRepository extends JpaRepository<UserClickDetail, Integer> {
    //用Optional儲存可能為空的值
    Optional<UserClickDetail> findByUserId(Integer userId);

}
