package com.hn.api.diary.dto.freeBoard;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FreeBoardCommentWriteDTO {
    private String postId;
    private String content;

    @Builder
    public FreeBoardCommentWriteDTO(String postId, String content) {
        this.postId = postId;
        this.content = content;
    }
}
