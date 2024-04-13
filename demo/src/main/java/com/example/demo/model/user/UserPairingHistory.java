package com.example.demo.model.user;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class UserPairingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user1_id")
    private Integer user1Id;
    @Column(name = "user2_id")
    private Integer user2Id;
    private Timestamp timestamp;
}

