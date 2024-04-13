package com.example.demo.model.user;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}


