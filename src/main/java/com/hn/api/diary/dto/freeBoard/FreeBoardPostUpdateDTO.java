package com.hn.api.diary.dto.freeBoard;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FreeBoardPostUpdateDTO {

    private String postId;
    @NotBlank(message = "Enter the title")
    private String title;
    private String content;

    @Builder // for test code
    public FreeBoardPostUpdateDTO(String postId, String title, String content) {
        this.postId = postId;
        this.title = title;
        this.content = content;
    }
}
