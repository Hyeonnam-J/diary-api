package com.hn.api.diary.dto.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SignInDTO {

    private String email;
    private String password;

    // 테스트 코드
    @Builder
    public SignInDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
