package com.hn.api.diary.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@NoArgsConstructor
@Getter
public class FreeBoardComment extends DateColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private FreeBoardPost freeBoardPost;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User user;

    private String content;
    private boolean isDelete;

    private Long origin;
    private Integer depth;

    @Builder
    public FreeBoardComment(FreeBoardPost freeBoardPost, User user, String content, Long origin, Integer depth) {
        this.freeBoardPost = freeBoardPost;
        this.user = user;
        this.content = content;

        this.isDelete = false;
        this.origin = origin;
        this.depth = depth != null ? depth : 0;
    }

    public void setOrigin(Long origin) {
        this.origin = origin;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}