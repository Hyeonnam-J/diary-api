package com.hn.api.diary.dto.user;

import com.hn.api.diary.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO extends User {
    private Long id;
    private String email;
    private String nick;
}
