package com.hn.api.diary.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpDTO {

    private String email;
    private String password;
    private String userName;
    private String nick;
    private String phoneNumber;
    private final String role = "USER";

    @Builder // for testcode
    public SignUpDTO(String email, String password, String userName, String nick, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.nick = nick;
        this.phoneNumber = phoneNumber;
    }
}
