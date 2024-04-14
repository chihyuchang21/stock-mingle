package com.example.demo.model.message;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userPairingHistoryId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer senderUserId;
    private Integer receiverUserId;

    private Timestamp sendTime;
}
