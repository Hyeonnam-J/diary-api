package com.hn.api.diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BoardPostWriteDTO {

    private String title;
    private String content;

    @Builder
    public BoardPostWriteDTO(String title, String content) {
        this.title = title;
        this.content = content;
    }
}