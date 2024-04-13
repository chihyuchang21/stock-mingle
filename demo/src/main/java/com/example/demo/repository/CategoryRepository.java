package com.example.demo.repository;

import com.example.demo.model.article.Category;
import com.example.demo.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {
    //找FK給Article對應存入
    Category findByCategory(String category);
}
