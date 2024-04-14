package com.example.demo.repository;

import com.example.demo.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAll();

//    NativeQuery
    @Query("SELECT new User(u.id, u.accountName, u.nickname, u.genderId) FROM User u")
    List<User> getAllUsersWithoutFavoriteTopicAndImage();

}

