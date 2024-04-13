package com.example.demo.repository;

import com.example.demo.model.stock.StockInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockInformationRepository extends JpaRepository<StockInformation, Long> {
}
