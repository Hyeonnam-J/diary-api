package com.hn.api.diary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

//    @ManyToOne
//    private User user;
//
//    @Builder
//    public MySession(User user) {
//        this.token = UUID.randomUUID().toString();
//        this.user = user;
//    }
}
