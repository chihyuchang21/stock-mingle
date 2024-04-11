package com.example.demo.model.stock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
public class StockInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String Name;
    private String Value;
    private LocalDateTime timestamp;
}
