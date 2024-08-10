package com.example.demo.repository;

import com.example.demo.model.article.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find the FK to store corresponding to the Article.
    Category findByCategory(String category);
}
